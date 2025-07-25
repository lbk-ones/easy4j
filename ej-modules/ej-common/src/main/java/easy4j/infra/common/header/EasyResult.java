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
package easy4j.infra.common.header;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.i18n.I18nUtils;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.common.utils.json.JacksonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jodd.util.StringPool;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 统一返回消息体
 *
 * @param <T>
 * @author bokun.li
 */
@Setter
@Getter
@Schema(description = "通用信息返回实体", name = "EasyResult")
public class EasyResult<T> implements Serializable {

    private static final long serialVersionUID = 6095433538316185020L;
    // 1 代表错误 0 代表正常返回
    @Schema(description = "1 代表错误 0 代表正常返回")
    private int error;

    // 业务状态码
    @Schema(description = "业务状态码")
    private String code;

    @JsonIgnore
    @Schema(description = "远程调用方法")
    private String rpcMethod;

    // 提示消息
    @Schema(description = "提示消息")
    private String message;

    // 错误堆栈信息
    @Schema(description = "错误堆栈信息")
    private String errorInfo;
    // 返回对象
    @Schema(description = "返回对象")
    private T data;


    public static <T> EasyResult<T> ok(T data) {

        EasyResult<T> easyResult = new EasyResult<T>();

        easyResult.setData(data);
        easyResult.setMessage(I18nUtils.getOperateSuccessStr());
        return easyResult;
    }

    public static <T> EasyResult<T> okCode(String code) {
        EasyResult<T> easyResult = new EasyResult<T>();
        easyResult.setData(null);
        easyResult.setCode(code);
        String message1 = I18nUtils.getMessage(code);
        if (StrUtil.isNotBlank(message1)) {
            easyResult.setMessage(message1);
        } else {
            easyResult.setMessage(I18nUtils.getOperateSuccessStr());
        }
        return easyResult;
    }

    public static <T> EasyResult<T> ok(T data, String code) {

        EasyResult<T> easyResult = new EasyResult<T>();

        easyResult.setData(data);
        easyResult.setCode(code);
        easyResult.setMessage(I18nUtils.getOperateSuccessStr());
        return easyResult;
    }

    @JsonIgnore
    public static <T> EasyResult<T> error(String message) {

        EasyResult<T> easyResult = new EasyResult<T>();
        easyResult.setError(SysConstant.ERRORCODE);
        easyResult.setMessage(message);
        easyResult.setData(null);
        return easyResult;
    }

    public static <T> EasyResult<T> parseFromI18n(int error, String i18nCode, T data) {

        EasyResult<T> easyResult = new EasyResult<T>();
        easyResult.setError(error);
        easyResult.setCode(i18nCode);
        easyResult.setMessage(I18nUtils.getMessage(i18nCode));
        easyResult.setData(data);
        return easyResult;
    }

    public static <T> EasyResult<T> parseFromI18n(int error, String i18nCode, String... args) {

        EasyResult<T> easyResult = new EasyResult<T>();
        easyResult.setError(error);
        easyResult.setCode(i18nCode);
        easyResult.setMessage(I18nUtils.getMessage(i18nCode, args));
        easyResult.setData(null);
        return easyResult;
    }

    @JsonIgnore
    public static <T> EasyResult<T> error(Throwable e) {
        EasyResult<T> easyResult = new EasyResult<T>();
        easyResult.setCode(BusCode.A00003);
        easyResult.setError(SysConstant.ERRORCODE);
        easyResult.setMessage(I18nUtils.getOperateErrorStr());
        if (!(e instanceof EasyException)) {
            easyResult.setErrorInfo(SysLog.getStackTraceInfo(e));
        }
        easyResult.setData(null);
        return easyResult;
    }

    /**
     * 接收rpc的报错异常
     *
     * @param e
     * @param <T>
     * @return
     */
    public static <T> EasyResult<T> rpcErrorInfo(Exception e) {
        EasyResult<T> easyResult = new EasyResult<>();
        easyResult.setCode("A00003");
        String message1 = e.getMessage();
        easyResult.setMessage(message1);
        easyResult.setError(SysConstant.ERRORCODE);
        easyResult.setErrorInfo("");
        easyResult.setData(null);
        return easyResult;
    }

    /**
     * <p>转 i18n</p>
     * <p>可以直接抛出类似这种异常 throw EasyException("A0001,参数1,参数2") 然后参数自动填充到占位符里面去</p>
     *
     * @param e   异常信息
     * @param <T>
     * @return 返回异常结果
     * @author bokun.li
     */
    public static <T> EasyResult<T> toI18n(Throwable e) {
        return toI18n(e, null);
    }

    /**
     * 根据传入的local转i18n
     *
     * @param e
     * @param local
     * @param <T>
     * @return
     * @author bokun.li
     */
    public static <T> EasyResult<T> toI18n(Throwable e, Locale local) {
        String msg = "";
        boolean isEasy4j = false;
        String msgKey = null;
        if (e instanceof EasyException) {
            String message1 = e.getMessage();
            if (StrUtil.isNotEmpty(message1)) {
                isEasy4j = true;
                int i = message1.indexOf(",");
                msgKey = message1.substring(0, i > 0 ? i : message1.length());
                if (i > 0) {
                    String argStr = message1.substring(i + 1);
                    if (StrUtil.isNotEmpty(argStr)) {
                        List<String> list = ListTs.asList(argStr.split(StringPool.COMMA));
                        msg = I18nUtils.getMessage(msgKey, local, list.toArray(new String[]{}));
                    } else {
                        // fix like this A00003,
                        String msg2 = StrUtil.replaceLast(message1, ",", "");
                        msg = I18nUtils.getMessage(msg2, local);
                    }
                } else {
                    msg = I18nUtils.getMessage(msgKey);
                }
            }
        }
        String code = "A00003";
        // 不允许使用自己定义的内容发布异常
        if (msg.isEmpty()) {
            msg = isEasy4j ? e.getMessage() : I18nUtils.getMessage(code, local);
        } else {
            code = msgKey;
        }
        EasyResult<T> easyResult = new EasyResult<T>();
        easyResult.setError(SysConstant.ERRORCODE);
        easyResult.setMessage(msg);
        easyResult.setCode(code);
        if (!(e instanceof EasyException)) {
            easyResult.setErrorInfo(SysLog.getStackTraceInfo(e));
        }
        easyResult.setData(null);
        return easyResult;

    }

    public EasyResult() {
        setError(SysConstant.SUCCESSCODE);
        setCode("A00001");
        //setMessage(I18nBean.getOperateSuccessStr());
    }


    @JsonIgnore
    public boolean isSuccess() {
        return error == 0;
    }

    public EasyResult(int error, String code, String message, T data) {
        this.error = error;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public EasyResult(int error) {
        this.error = error;
    }

    public EasyResult(int error, String code) {
        this.error = error;
        this.code = code;
    }

    public EasyResult(int error, String code, String message) {
        this.error = error;
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return JacksonUtil.toJsonContainNull(this);
    }

    /**
     * 兼容获取消息和错误
     *
     * @author bokun.li
     * @date 2025-06-15
     */
    @JsonIgnore
    public String getMsgAndError() {
        String message1 = StrUtil.blankToDefault(this.getMessage(), "");
        String error1 = StrUtil.blankToDefault(this.getErrorInfo(), "");
        return StrUtil.blankToDefault(message1, "") + (StrUtil.isNotBlank(error1) ? ":" + error1 : "");
    }
}
