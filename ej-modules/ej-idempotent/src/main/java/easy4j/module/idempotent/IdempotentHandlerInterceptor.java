/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.idempotent;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.THConstant;
import easy4j.infra.context.api.idempotent.Easy4jIdempotentKeyGenerator;
import easy4j.infra.context.api.idempotent.Easy4jIdempotentStorage;
import easy4j.infra.webmvc.AbstractEasy4JWebMvcHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;


/**
 * IdempotentHandlerInterceptor
 *
 * @author bokun.li
 * @date 2025-05
 */
//@Component("idempotentHandlerInterceptor")
@Slf4j
public class IdempotentHandlerInterceptor extends AbstractEasy4JWebMvcHandler {

    public static final String IDENTIFY_KEY = "webIdempotentKeyValue";
    public static final String WEB_ANNOTATION_KEY = "webIdempotent";

    volatile IdempotentToolFactory idempotentToolFactory;

    public IdempotentToolFactory idempotentToolFactory() {
        if (idempotentToolFactory == null) {
            synchronized (IdempotentHandlerInterceptor.class) {
                if (idempotentToolFactory == null) {
                    idempotentToolFactory = SpringUtil.getBean(IdempotentToolFactory.class);
                }
            }
        }
        return idempotentToolFactory;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handler) {
        Method method = handler.getMethod();
        if (method.isAnnotationPresent(WebIdempotent.class)) {
            WebIdempotent annotation = method.getAnnotation(WebIdempotent.class);
            String keyType = annotation.keyGeneratorType();
            StorageTypeEnum storageType = annotation.storageType();
            IdempotentToolFactory idempotentedToolFactory = idempotentToolFactory();
            Easy4jIdempotentKeyGenerator keyGenerator = idempotentedToolFactory.getKeyGenerator(keyType);
            Easy4jIdempotentStorage storage = idempotentedToolFactory.getStorage(storageType);
            boolean globalIdempotent = annotation.globalIdempotent();
            boolean degradeGlobalIdempotent = annotation.degradeGlobalIdempotent();
            String generateKey = unionKey(globalIdempotent, degradeGlobalIdempotent, request, keyGenerator.generate(request));
            request.setAttribute(WEB_ANNOTATION_KEY, annotation);
            request.setAttribute(IDENTIFY_KEY, generateKey);
            if (!storage.acquireLock(generateKey, annotation.expireSeconds(), request)) {
                throw EasyException.wrap(BusCode.A00021, generateKey);
            }
        }
        return true;
    }


    /**
     * 获取默认值
     * token + methodType + uri
     *
     * @author bokun.li
     * @date 2025/7/8
     */
    private String unionKey(boolean globalIdempotent, boolean degradeGlobalIdempotent, HttpServletRequest request, String generateKey) {
        String requestURI = request.getRequestURI();
        String method2 = request.getMethod();
        String generateKey2 = method2 + "--" + requestURI;
        if (globalIdempotent) {
            return generateKey2;
        }
        if (StrUtil.isBlank(generateKey)) {
            Easy4jContext context = Easy4j.getContext();
            boolean isNoLogin = false;
            Optional<Object> threadHashValue = context.getThreadHashValue(THConstant.EASY4J_IS_NO_LOGIN, THConstant.EASY4J_IS_NO_LOGIN);
            if (threadHashValue.isPresent()) {
                // nologin will be ignore
                if ((boolean) threadHashValue.get()) {
                    isNoLogin = true;
                }
            }
            // global idempotent
            if (degradeGlobalIdempotent || isNoLogin) {
                return generateKey2;
            }
            String accessToken = request.getHeader(SysConstant.X_ACCESS_TOKEN);
            if (StrUtil.isBlank(accessToken)) {
                return generateKey;
            }
            return accessToken + "--" + generateKey2;
        }
        return generateKey;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Exception ex, HandlerMethod handler) {
        Object webIdempotent = request.getAttribute(WEB_ANNOTATION_KEY);
        if (webIdempotent == null) {
            return;
        }
        if (webIdempotent instanceof Annotation) {
            WebIdempotent annotation = (WebIdempotent) webIdempotent;
            Object webIdempotentKeyValue = request.getAttribute(IDENTIFY_KEY);
            Object attribute = request.getAttribute(Easy4jIdempotentStorage.IS_LOCK);
            if (StrUtil.isBlankIfStr(webIdempotentKeyValue) || !"1".equals(attribute)) {
                log.warn("no lock but aquire release lock" + webIdempotentKeyValue);
                return;
            }
            Easy4jIdempotentStorage storage;
            try {
                storage = idempotentToolFactory().getStorage(annotation.storageType());
            } catch (Exception e) {
                log.error(SysLog.compact(e.getMessage()));
                return;
            }
            storage.releaseLock(webIdempotentKeyValue.toString());
        }
    }
}
