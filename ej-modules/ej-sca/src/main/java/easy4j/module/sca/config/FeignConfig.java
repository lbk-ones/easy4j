package easy4j.module.sca.config;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import easy4j.module.base.properties.EjProperties;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.sca.util.HttpUtils;
import easy4j.module.sca.util.PathMatcherUtil;
import easy4j.module.sca.context.TenantContext;
import easy4j.module.sca.context.UserTokenContext;
import easy4j.module.sca.util.SignUtil;
import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;

@ConditionalOnClass(Feign.class)
@AutoConfigureBefore(FeignAutoConfiguration.class)
@Slf4j
@Configuration
public class FeignConfig {
    @Resource
    EjProperties ejProperties;


    /**
     * 设置feign header参数
     * 【X_ACCESS_TOKEN】【X_SIGN】【X_TIMESTAMP】
     *
     * @return
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (null != attributes) {
                HttpServletRequest request = attributes.getRequest();
                log.debug("Feign request: {}", request.getRequestURI());
                // 将token信息放入header中
                String token = request.getHeader(SysConstant.X_ACCESS_TOKEN);
                if (token == null || token.isEmpty()) {
                    token = request.getParameter("token");
                    //免Token
                    if (StringUtils.isEmpty(token)) {
                        token = UserTokenContext.getToken();
                    }
                }
                log.debug("Feign Login Request token: {}", token);
                requestTemplate.header(SysConstant.X_ACCESS_TOKEN, token);

                // 将tenantId信息放入header中
                String tenantId = request.getHeader(SysConstant.TENANT_ID);
                if (tenantId == null || tenantId.isEmpty()) {
                    tenantId = request.getParameter(SysConstant.TENANT_ID);
                }
                log.debug("Feign Login Request tenantId: {}", tenantId);
                requestTemplate.header(SysConstant.TENANT_ID, tenantId);

            } else {
                String token = UserTokenContext.getToken();
                log.debug("Feign no Login token: {}", token);
                requestTemplate.header(SysConstant.X_ACCESS_TOKEN, token);

                String tenantId = TenantContext.getTenant();
                log.debug("Feign no Login tenantId: {}", tenantId);
                requestTemplate.header(SysConstant.TENANT_ID, tenantId);
            }

            //================================================================================================================
            //针对特殊接口，进行加签验证 ——根据URL地址过滤请求 【字典表参数签名验证】

            //1.查询需要进行签名拦截的接口 signUrls
            String signUrls = ejProperties.getSignUrls();
            List<String> signUrlsArray = null;
            if (StringUtils.isNotBlank(signUrls)) {
                signUrlsArray = Arrays.asList(signUrls.split(","));
            } else {
                return;
            }
            //2.拦截处理，加入签名逻辑
            if (PathMatcherUtil.matches(signUrlsArray, requestTemplate.path())) {
                try {
                    log.debug("============================ [begin] fegin starter url ============================");
                    log.debug(requestTemplate.path());
                    log.debug(requestTemplate.method());
                    String queryLine = requestTemplate.queryLine();
                    if (queryLine.startsWith("?")) {
                        queryLine = queryLine.substring(1);
                    }
                    log.debug(queryLine);
                    if (requestTemplate.body() != null) {
                        log.debug(new String(requestTemplate.body()));
                    }
                    SortedMap<String, String> allParams = HttpUtils.getAllParams(requestTemplate.path(), queryLine, requestTemplate.body(), requestTemplate.method());
                    String sign = SignUtil.getParamsSign(allParams);
                    log.info("【微服务】 Feign request params sign: {}", sign);
                    log.debug("============================ [end] fegin starter url ============================");
                    requestTemplate.header(SysConstant.X_SIGN, sign);
                    requestTemplate.header(SysConstant.X_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //================================================================================================================
        };
    }


    /**
     * Feign 客户端的日志记录，默认级别为NONE
     * Logger.Level 的具体级别如下：
     * NONE：不记录任何信息
     * BASIC：仅记录请求方法、URL以及响应状态码和执行时间
     * HEADERS：除了记录 BASIC级别的信息外，还会记录请求和响应的头信息
     * FULL：记录所有请求与响应的明细，包括头信息、请求体、元数据
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * Feign支持文件上传
     *
     * @param messageConverters
     * @return
     */
    @Bean
    @Primary
    @Scope("prototype")
    public Encoder multipartFormEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }

    @Bean
    public Encoder feignEncoder() {
        return new SpringEncoder(feignHttpMessageConverter());
    }

    @Bean
    public Decoder feignDecoder() {
        return new SpringDecoder(feignHttpMessageConverter());
    }

    /**
     * 设置解码器为fastjson
     *
     * @return
     */
    private ObjectFactory<HttpMessageConverters> feignHttpMessageConverter() {
        final HttpMessageConverters httpMessageConverters = new HttpMessageConverters(this.getFastJsonConverter());
        return () -> httpMessageConverters;
    }

    private FastJsonHttpMessageConverter getFastJsonConverter() {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();

        List<MediaType> supportedMediaTypes = new ArrayList<>();
        MediaType mediaTypeJson = MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE);
        supportedMediaTypes.add(mediaTypeJson);
        converter.setSupportedMediaTypes(supportedMediaTypes);
        FastJsonConfig config = new FastJsonConfig();
//        config.getSerializeConfig().put(JSON.class, new SwaggerJsonSerializer());
        config.setSerializerFeatures(SerializerFeature.DisableCircularReferenceDetect);
        converter.setFastJsonConfig(config);

        return converter;
    }

}
