/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package easy4j.infra.webmvc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * 参考下 本来想改写下 然后用这个拦截器 后来并没有
 * <br/>
 * <br/>
 * Interceptor that allows for changing the current locale on every request,
 * via a configurable request parameter (default parameter name: "locale").
 *
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @see LocaleResolver
 * @since 20.06.2003
 */
public class LocaleChangeInterceptor2 implements HandlerInterceptor {

    /**
     * Default name of the locale specification parameter: "locale".
     */
    public static final String DEFAULT_PARAM_NAME = "locale";


    protected final Log logger = LogFactory.getLog(getClass());

    private String paramName = DEFAULT_PARAM_NAME;

    @Nullable
    private String[] httpMethods;

    private boolean ignoreInvalidLocale = false;


    /**
     * Set the name of the parameter that contains a locale specification
     * in a locale change request. Default is "locale".
     */
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    /**
     * Return the name of the parameter that contains a locale specification
     * in a locale change request.
     */
    public String getParamName() {
        return this.paramName;
    }

    /**
     * Configure the HTTP method(s) over which the locale can be changed.
     *
     * @param httpMethods the methods
     * @since 4.2
     */
    public void setHttpMethods(@Nullable String... httpMethods) {
        this.httpMethods = httpMethods;
    }

    /**
     * Return the configured HTTP methods.
     *
     * @since 4.2
     */
    @Nullable
    public String[] getHttpMethods() {
        return this.httpMethods;
    }

    /**
     * Set whether to ignore an invalid value for the locale parameter.
     *
     * @since 4.2.2
     */
    public void setIgnoreInvalidLocale(boolean ignoreInvalidLocale) {
        this.ignoreInvalidLocale = ignoreInvalidLocale;
    }

    /**
     * Return whether to ignore an invalid value for the locale parameter.
     *
     * @since 4.2.2
     */
    public boolean isIgnoreInvalidLocale() {
        return this.ignoreInvalidLocale;
    }

    /**
     * Specify whether to parse request parameter values as BCP 47 language tags
     * instead of Java's legacy locale specification format.
     * <p><b>NOTE: As of 5.1, this resolver leniently accepts the legacy
     * {@link Locale#toString} format as well as BCP 47 language tags.</b>
     *
     * @see Locale#forLanguageTag(String)
     * @see Locale#toLanguageTag()
     * @since 4.3
     * @deprecated as of 5.1 since it only accepts {@code true} now
     */
    @Deprecated
    public void setLanguageTagCompliant(boolean languageTagCompliant) {
        if (!languageTagCompliant) {
            throw new IllegalArgumentException("LocaleChangeInterceptor always accepts BCP 47 language tags");
        }
    }

    /**
     * Return whether to use BCP 47 language tags instead of Java's legacy
     * locale specification format.
     *
     * @since 4.3
     * @deprecated as of 5.1 since it always returns {@code true} now
     */
    @Deprecated
    public boolean isLanguageTagCompliant() {
        return true;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws ServletException {

        String newLocale = request.getParameter(getParamName());
        if (newLocale != null) {
            if (checkHttpMethod(request.getMethod())) {
                LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
                if (localeResolver == null) {
                    throw new IllegalStateException(
                            "No LocaleResolver found: not in a DispatcherServlet request?");
                }
                try {
                    localeResolver.setLocale(request, response, parseLocaleValue(newLocale));
                } catch (IllegalArgumentException ex) {
                    if (isIgnoreInvalidLocale()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Ignoring invalid locale value [" + newLocale + "]: " + ex.getMessage());
                        }
                    } else {
                        throw ex;
                    }
                }
            }
        }
        // Proceed in any case.
        return true;
    }

    private boolean checkHttpMethod(String currentMethod) {
        String[] configuredMethods = getHttpMethods();
        if (ObjectUtils.isEmpty(configuredMethods)) {
            return true;
        }
        for (String configuredMethod : configuredMethods) {
            if (configuredMethod.equalsIgnoreCase(currentMethod)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parse the given locale value as coming from a request parameter.
     * <p>The default implementation calls {@link StringUtils#parseLocale(String)},
     * accepting the {@link Locale#toString} format as well as BCP 47 language tags.
     *
     * @param localeValue the locale value to parse
     * @return the corresponding {@code Locale} instance
     * @since 4.3
     */
    @Nullable
    protected Locale parseLocaleValue(String localeValue) {
        return StringUtils.parseLocale(localeValue);
    }

}
