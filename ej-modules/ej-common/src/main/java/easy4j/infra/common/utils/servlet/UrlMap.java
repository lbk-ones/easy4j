package easy4j.infra.common.utils.servlet;

import cn.hutool.http.ContentType;
import org.apache.poi.openxml4j.opc.ContentTypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlMap {


    /**
     * 必须完全匹配
     */
    String url() default "";

    /**
     * 匹配请求方法
     */
    MethodType method() default MethodType.POST;

    /**
     * 返回content-type 默认 json
     * @return
     */
    String returnContentType() default "application/json;charset=UTF-8";



}
