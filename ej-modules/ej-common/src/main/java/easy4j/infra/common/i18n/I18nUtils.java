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

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.SysLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.util.Locale;

/**
 * 一些系统工具
 *
 * @author bokun.li
 * @date 2023/11/23
 */
@Slf4j
public class I18nUtils implements InitializingBean {

    private static I18nBean i18nBean;

    @Resource
    public void setI18nBean(I18nBean i18NBean) {
        I18nUtils.i18nBean = i18NBean;
    }

    private static I18nBean getI18Bean() {
        if (i18nBean == null) {
            i18nBean = SpringUtil.getBean(I18nBean.class);
        }
        return i18nBean;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info(SysLog.compact("i18nUtils工具加载成功"));
    }


    public static String getOperateSuccessStr() {
        return clear(getI18Bean().getSysMessage(BusCode.A00001));
    }

    public static String getOperateErrorStr() {
        return clear(getI18Bean().getSysMessage(BusCode.A00002));
    }

    public static String getSysErrorStr() {
        return clear(getI18Bean().getSysMessage(BusCode.A00003));
    }

    public static String getMessage(String msgKey, String... paramster) {
        return clear(getI18Bean().getMessage(msgKey, paramster));
    }

    public static String getMessageByKey(String msgKey) {
        return getI18Bean().getMessage(msgKey);
    }

    public static String getMessage(String msgKey, Locale locale, String... paramster) {
        return clear(getI18Bean().getMessage(msgKey, locale, paramster));
    }

    public static String clear(String msg) {
        // 去掉占位符
        return StrUtil.isNotBlank(msg) ? msg.replaceAll("\\{\\d+\\}", "") : msg;
    }


}
