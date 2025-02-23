package easy4j.module.idempotent;

import easy4j.module.base.header.EasyResult;
import easy4j.module.base.utils.BusCode;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.base.plugin.idempotent.Easy4jIdempotentKeyGenerator;
import easy4j.module.base.plugin.idempotent.Easy4jIdempotentStorage;
import easy4j.module.base.utils.SysLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;


@Component("idempotentHandlerInterceptor")
@Slf4j
public class IdempotentHandlerInterceptor implements HandlerInterceptor {

    public static final String IDENTIFY_KEY = "webIdempotentKeyValue";
    public static final String WEB_ANNOTATION_KEY = "webIdempotent";


    @Autowired
    IdempotentToolFactory idempotentToolFactory;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handler1 = (HandlerMethod) handler;
            Method method = handler1.getMethod();
            if (method.isAnnotationPresent(WebIdempotent.class)) {
                WebIdempotent annotation = method.getAnnotation(WebIdempotent.class);
                String s = annotation.keyGeneratorType();
                StorageTypeEnum storageType = annotation.storageType();
                Easy4jIdempotentKeyGenerator keyGenerator = idempotentToolFactory.getKeyGenerator(s);
                Easy4jIdempotentStorage storage;
                try{
                    storage = idempotentToolFactory.getStorage(storageType);
                }catch (Exception e){
                    log.error(SysLog.compact(e.getMessage()));
                    return true;
                }
                String generate = keyGenerator.generate(request);
                request.setAttribute(WEB_ANNOTATION_KEY,annotation);
                request.setAttribute(IDENTIFY_KEY,generate);

                if (!storage.acquireLock(generate, annotation.expireSeconds())) {
                    PrintWriter writer = response.getWriter();
                    writer.write(EasyResult.parseFromI18n(SysConstant.ERRORCODE,BusCode.A00021).toString());
                    writer.flush();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Object webIdempotent = request.getAttribute(WEB_ANNOTATION_KEY);
        if(webIdempotent instanceof Annotation){
            WebIdempotent annotation = (WebIdempotent) webIdempotent;
            String webIdempotentKeyValue = String.valueOf(request.getAttribute(IDENTIFY_KEY));
            if(webIdempotentKeyValue == null){
                return;
            }
            Easy4jIdempotentStorage storage;
            try{
                storage = idempotentToolFactory.getStorage(annotation.storageType());
            }catch (Exception e){
                log.error(SysLog.compact(e.getMessage()));
                return;
            }
            storage.releaseLock(webIdempotentKeyValue);
        }
    }
}
