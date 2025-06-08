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
package easy4j.infra.common.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Objects;

/**
 * I18nUtils
 *
 * @author bokun.li
 * @date 2025-05
 */
public class I18nBean {
    private final MessageSource messageSource;

    public I18nBean(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String msgKey, Object[] args) {
        return messageSource.getMessage(msgKey, args, "", LocaleContextHolder.getLocale());
    }

    public String getMessage(String msgKey, Locale locale, Object[] args) {
        return messageSource.getMessage(msgKey, args, "", Objects.isNull(locale) ? LocaleContextHolder.getLocale() : locale);
    }

    public String getMessage(String msgKey) {
        return messageSource.getMessage(msgKey, null, "", LocaleContextHolder.getLocale());
    }

    public String getSysMessage(String msgKey) {
        return messageSource.getMessage(msgKey, null, "", LocaleContextHolder.getLocale());
    }
}
