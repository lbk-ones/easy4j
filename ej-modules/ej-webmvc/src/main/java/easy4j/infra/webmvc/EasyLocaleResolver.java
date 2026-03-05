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
package easy4j.infra.webmvc;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Locale;

/**
 * 只有这样才能覆盖 WebMvcAutoConfiguration里面的一个Bean
 *
 * @author bokun.li
 * @date 2023/11/23
 */
//@Component("localeResolver")
public class EasyLocaleResolver implements LocaleResolver, InitializingBean {

    private Logger log = LoggerFactory.getLogger(EasyLocaleResolver.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info(SysLog.compact("自定义local初始化"));
    }

    @Resource
    private HttpServletRequest request;

    public Locale getLocal() {
        return resolveLocale(request);
    }


    /**
     * 从HttpServletRequest中获取Locale
     *
     * @param httpServletRequest httpServletRequest
     * @return 语言Local
     */
    @Override
    public Locale resolveLocale(HttpServletRequest httpServletRequest) {
        //获取请求中的语言参数
        String lang1 = httpServletRequest.getParameter("lang");
        String lang2 = httpServletRequest.getHeader("lang");
        String language = StrUtil.blankToDefault(lang1, lang2);
        //如果没有就使用默认的（根据主机的语言环境生成一个 Locale
        if (StrUtil.isBlank(language)) {
            language = Easy4j.getProperty(SysConstant.EASY4J_DEFAULT_I18N);
        }
        Locale locale = null;
        try {
            //如果请求的链接中携带了 国际化的参数
            if (!StrUtil.isEmpty(language)) {
                //zh_CN
                String[] s = language.split("_");
                //国家，地区
                locale = new Locale(s[0], s[1]);
            }
        } catch (Exception e) {
            locale = Locale.getDefault();
        }

        return locale;
    }

    /**
     * 用于实现Locale的切换。比如SessionLocaleResolver获取Locale的方式是从session中读取，但如果
     * 用户想要切换其展示的样式(由英文切换为中文)，那么这里的setLocale()方法就提供了这样一种可能
     *
     * @param request             HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @param locale              locale
     */
    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse httpServletResponse, Locale locale) {
        Locale locale1 = LocaleContextHolder.getLocale();
        if (!locale1.equals(locale)) {
            LocaleContextHolder.setLocale(locale);
        }
    }

}
