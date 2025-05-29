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
package easy4j.module.base.plugin.i18n;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * i18n语言配置切换
 * 请求参数 请求体添加 lang参数 zh_CN 汉语 en_US 英文 其他暂时还没考虑但是增加语言很简单
 *
 * @author bokun.li
 * @date 2023/11/23
 */
@Configuration
@AutoConfigureBefore(value = {WebMvcAutoConfiguration.class})
public class Config {

    /**
     * 默认解析器 其中locale表示默认语言
     */
    @Bean("localeResolver")
    public LocaleResolver localeResolver() {
        return new EasyLocaleResolver();
    }


    /*@Bean
    @ConfigurationProperties(prefix = "spring.messages")
    public MessageSourceProperties messageSourceProperties() {
        return new MessageSourceProperties();
    }
*/
    @Bean
    public I18nUtils i18nUtils() {
        return new I18nUtils(messageSource());
    }

    @Bean
    public I18nBean i18nBean(){
        return new I18nBean();
    }

    /**
     *
     **/
    @Bean(name = "messageSource")
    public ReloadableResourceBundleMessageSource messageSource() {

        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasenames("classpath:/i18n/sys","classpath:/i18n/messages");
        //默认是false，调试设置为true 打开之后
//        source.setUseCodeAsDefaultMessage(true);
        source.setDefaultEncoding(StandardCharsets.UTF_8.displayName(Locale.getDefault()));
        source.setDefaultLocale(new Locale("zh","CN"));
        return source;
    }


    /**
     * 资源文件路径
     *
     * @param properties
     * @return
     */
    /*@Bean(name = "messageSource")
    public ResourceBundleMessageSource messageSource(MessageSourceProperties properties) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("i18n/messages","i18n/sysmsg");

        if (properties.getEncoding() != null) {
            messageSource.setDefaultEncoding(properties.getEncoding().name());
        }else{
            Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
        }
        messageSource.setFallbackToSystemLocale(properties.isFallbackToSystemLocale());
        Duration cacheDuration = properties.getCacheDuration();
        if (cacheDuration != null) {
            long seconds = cacheDuration.getSeconds();
            if(seconds!=0){
                messageSource.setCacheSeconds((int) seconds);
            }
        }else{
            // 两分钟同步一次 如果没有更改过也不会真的去获取 只是尝试着去获取
            messageSource.setCacheSeconds(120);
        }
        messageSource.setAlwaysUseMessageFormat(properties.isAlwaysUseMessageFormat());
        messageSource.setUseCodeAsDefaultMessage(properties.isUseCodeAsDefaultMessage());
        return messageSource;
    }*/



    /**
     * 性感拦截器 在线切换语言模板
     * 默认拦截器 其中lang表示切换语言的参数名
     */
    @Bean
    public WebMvcConfigurer localeInterceptor() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                LocaleChangeInterceptor localeInterceptor = new LocaleChangeInterceptor();
                localeInterceptor.setParamName("lang");
                registry.addInterceptor(localeInterceptor);
            }
        };
    }
}
