package easy4j.module.base.plugin.i18n;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.utils.SysLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.util.Locale;

/**
 * 一些系统工具
 * @author bokun.li
 * @date 2023/11/23
 */
@Slf4j
public class I18nBean implements InitializingBean {

    public static I18nUtils i18nUtils;

    @Resource
    public void setI18nUtils(I18nUtils i18nUtils) {
        I18nBean.i18nUtils = i18nUtils;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info(SysLog.compact("i18nUtils工具加载成功"));
    }


    public static String getOperateSuccessStr(){
        return clear(i18nUtils.getSysMessage("A00001"));
    }

    public static String getOperateErrorStr(){
        return clear(i18nUtils.getSysMessage("A00002"));
    }

    public static String getSysErrorStr(){
        return clear(i18nUtils.getSysMessage("A00003"));
    }

    public static String getMessage(String msgKey,String...paramster){
        return clear(i18nUtils.getMessage(msgKey,paramster));
    }
    public static String getMessageByKey(String msgKey){
        return i18nUtils.getMessage(msgKey);
    }

    public static String getMessage(String msgKey, Locale locale, String...paramster){
        return clear(i18nUtils.getMessage(msgKey,locale,paramster));
    }

    public static String clear(String msg){
        // 去掉占位符
        return StrUtil.isNotBlank(msg)?msg.replaceAll("\\{\\d+\\}", ""):msg;
    }



}
