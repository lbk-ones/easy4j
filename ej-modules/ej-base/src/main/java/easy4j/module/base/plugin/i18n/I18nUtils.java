package easy4j.module.base.plugin.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Objects;

public class I18nUtils {
   private final MessageSource messageSource;

   public I18nUtils(MessageSource messageSource) {
       this.messageSource = messageSource;
   }

   public String getMessage(String msgKey, Object[] args) {
       return messageSource.getMessage(msgKey, args,"", LocaleContextHolder.getLocale());
   }
    public String getMessage(String msgKey, Locale locale, Object[] args) {
        return messageSource.getMessage(msgKey, args,"", Objects.isNull(locale)?LocaleContextHolder.getLocale():locale);
    }

    public String getMessage(String msgKey) {
        return messageSource.getMessage(msgKey, null,"", LocaleContextHolder.getLocale());
    }

   public String getSysMessage(String msgKey) {
       return messageSource.getMessage(msgKey, null,"", LocaleContextHolder.getLocale());
   }
}