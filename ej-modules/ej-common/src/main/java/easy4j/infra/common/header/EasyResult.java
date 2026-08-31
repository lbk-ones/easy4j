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
import easy4j.infra.common.utils.*;
import easy4j.infra.common.utils.json.JacksonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jodd.util.StringPool;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

/**
 * 统一返回消息体（HashMap 版本）参数名称可以变更以此来适应各个系统的返回响应值名称
 *
 * @param <T>
 * @author bokun.li
 */
@Schema(
        description = "通用信息返回实体",
        name = "EasyResult",
        example = """
                {
                  "code": "0",
                  "message": "操作成功",
                  "data": null,
                  "errorInfo": null,
                  "traceId":"",
                  "cost:250
                }
                """
)
public class EasyResult<T> extends EasyMap<String, Object> implements Serializable {

    /**
     * 这些参数名称，可以全局更改一次
     */
    @Schema(hidden = true)
    private static String CODE = "code";

    @Schema(hidden = true)
    private static String MESSAGE = "message";

    @Schema(hidden = true)
    private static String DATA = "data";

    @Schema(hidden = true)
    private static String RPC_METHOD = "rpcMethod";

    @Schema(hidden = true)
    private static String ERROR_INFO = "errorInfo";

    @Schema(hidden = true)
    private static String TRACE_ID = "traceId";


    @Schema(hidden = true)
    private static String COST = "cost";

    @Serial
    private static final long serialVersionUID = 6095433538316185020L;


    public static void setCODE(String CODE_) {
        EasyResult.CODE = CODE_;
    }

    public static void setMESSAGE(String MESSAGE_) {
        EasyResult.MESSAGE = MESSAGE_;
    }

    public static void setDATA(String DATA_) {
        EasyResult.DATA = DATA_;
    }

    public static void setRPC_METHOD(String RPC_METHOD_) {
        EasyResult.RPC_METHOD = RPC_METHOD_;
    }

    public static void setERROR_INFO(String ERROR_INFO_) {
        EasyResult.ERROR_INFO = ERROR_INFO_;
    }

    public static void setTRACE_ID(String TRACE_ID_) {
        EasyResult.TRACE_ID = TRACE_ID_;
    }

    public static void setCOST(String COST_) {
        EasyResult.COST = COST_;
    }


    public EasyResult() {
        this.setCode(String.valueOf(SysConstant.SUCCESS_CODE))
                .setMessage(I18nUtils.getOperateSuccessStr())
                .setData(null);
    }

    public EasyResult(String code, String message, T data) {
        this.setCode(code)
                .setMessage(message)
                .setData(data);
    }

    public EasyResult(String code) {
        this.setCode(code);
    }


    public EasyResult(String code, String message) {
        this.setCode(code)
                .setMessage(message);
    }

    /**
     * 获取业务状态码
     *
     * @return 业务状态码（0=成功，非0=错误）
     */
    @Schema(
            description = "业务状态码（0=成功，非0=错误）",
            example = "0",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    public String getCode() {
        Object obj = get(CODE);
        return obj != null ? obj.toString() : null;
    }

    /**
     * 设置业务状态码
     */
    public EasyResult<T> setCode(String code) {
        this.put(CODE, code);
        return this;
    }

    /**
     * 获取耗时时间
     *
     * @return 业务状态码（0=成功，非0=错误）
     */
    @Schema(
            description = "耗时时间",
            example = "0",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    public Long getCost() {
        Object obj = get(COST);
        return obj != null ? Long.parseLong(obj.toString()) : null;
    }

    /**
     * 设置耗时时间
     */
    public EasyResult<T> setCost(Long cost) {
        this.put(COST, cost);
        return this;
    }

    /**
     * 获取业务状态码
     *
     * @return 业务状态码（0=成功，非0=错误）
     */
    @Schema(
            description = "链路ID",
            example = "xxxx",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    public String getTraceId() {
        Object obj = get(CODE);
        return obj != null ? obj.toString() : null;
    }

    /**
     * 设置业务状态码
     */
    public EasyResult<T> setTraceId(String traceId) {
        this.put(TRACE_ID, traceId);
        return this;
    }

    /**
     * 获取远程调用方法（内部字段，不在 API 文档中显示）
     */
    @Schema(hidden = true)
    public String getRpcMethod() {
        Object obj = get(RPC_METHOD);
        return obj != null ? obj.toString() : null;
    }

    /**
     * 设置远程调用方法
     */
    public EasyResult<T> setRpcMethod(String rpcMethod) {
        this.put(RPC_METHOD, rpcMethod);
        return this;
    }

    /**
     * 获取提示消息
     *
     * @return 提示消息内容
     */
    @Schema(
            description = "提示消息",
            example = "操作成功",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    public String getMessage() {
        Object obj = get(MESSAGE);
        return obj != null ? obj.toString() : null;
    }

    /**
     * 设置提示消息
     */
    public EasyResult<T> setMessage(String message) {
        this.put(MESSAGE, message);
        return this;
    }

    /**
     * 获取错误堆栈信息
     *
     * @return 错误详细信息
     */
    @Schema(
            description = "错误堆栈信息",
            example = "java.lang.NullPointerException: ...",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    public String getErrorInfo() {
        Object obj = get(ERROR_INFO);
        return obj != null ? obj.toString() : null;
    }

    /**
     * 设置错误堆栈信息
     */
    public EasyResult<T> setErrorInfo(String errorInfo) {
        this.put(ERROR_INFO, errorInfo);
        return this;
    }

    /**
     * 获取返回对象
     *
     * @return 业务数据对象
     */
    @Schema(
            description = "返回对象/业务数据",
            example = "null",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    public T getData() {
        Object o = get(DATA);
        if (o != null) {
            return (T) o;
        }
        return null;
    }

    /**
     * 设置返回对象
     */
    public EasyResult<T> setData(T data) {
        this.put(DATA, data);
        return this;
    }

    /**
     * 默认code取1
     *
     * @param data 要返回的参数
     * @param <T>  约束泛型
     * @return EasyResult<T>
     */
    public static <T> EasyResult<T> ok(T data) {
        EasyResult<T> easyResult = new EasyResult<T>();
        easyResult.setData(data);
        return easyResult;
    }

    /**
     * 默认code取1
     *
     * @param data 要返回的参数
     * @param <T>  约束泛型
     * @return EasyResult<T>
     */
    public static <T> EasyResult<T> ok(T data, String message) {
        return new EasyResult<T>().setData(data).setMessage(message);
    }


    /**
     * 将code强制改为200
     *
     * @param data 要返回的值
     * @param <T>  泛型约束
     * @return EasyResult
     */
    public static <T> EasyResult<T> ok200(T data) {
        return new EasyResult<T>().setCode("200").setData(data);
    }

    public static <T> EasyResult<T> okCode(String code) {
        EasyResult<T> easyResult = new EasyResult<T>();
        easyResult.setCode(code);
        String message1 = I18nUtils.getMessage(code);
        if (StrUtil.isNotBlank(message1)) {
            easyResult.setMessage(message1);
        } else {
            easyResult.setMessage(I18nUtils.getOperateSuccessStr());
        }
        return easyResult;
    }

    public static <T> EasyResult<T> okCode(String code, T data) {
        EasyResult<T> easyResult = new EasyResult<T>();
        easyResult.setData(data);
        easyResult.setCode(code);
        String message1 = I18nUtils.getMessage(code);
        if (StrUtil.isNotBlank(message1)) {
            easyResult.setMessage(message1);
        } else {
            easyResult.setMessage(I18nUtils.getOperateSuccessStr());
        }
        return easyResult;
    }

    public static <T> EasyResult<T> okCode(String code, String message, T data) {
        EasyResult<T> easyResult = new EasyResult<T>()
                .setData(data)
                .setMessage(message)
                .setCode(code);
        String message1 = I18nUtils.getMessage(code);
        if (StrUtil.isNotBlank(message1)) {
            easyResult.setMessage(message1);
        } else {
            easyResult.setMessage(I18nUtils.getOperateSuccessStr());
        }
        return easyResult;
    }


    @JsonIgnore
    public static <T> EasyResult<T> error(String message) {
        EasyResult<T> easyResult = new EasyResult<T>();
        easyResult.setCode(BusCode.A00003);
        easyResult.setMessage(message);
        return easyResult;
    }

    public static <T> EasyResult<T> parseI18nWithData(String i18nCode, T data) {

        EasyResult<T> easyResult = new EasyResult<T>();
        easyResult.setCode(i18nCode);
        easyResult.setMessage(I18nUtils.getMessage(i18nCode));
        easyResult.setData(data);
        return easyResult;
    }

    public static <T> EasyResult<T> parseFromI18n(String i18nCode, String... args) {

        EasyResult<T> easyResult = new EasyResult<T>();
        easyResult.setCode(i18nCode);
        easyResult.setMessage(I18nUtils.getMessage(i18nCode, args));
        return easyResult;
    }


    // 失败返回（code ≠ 0）
    public static <T> EasyResult<T> error(String code, String message) {
        return new EasyResult<T>().setCode(code)
                .setMessage(message);
    }

    // 失败返回（带异常信息）
    public static <T> EasyResult<T> error(String code, String message, String errorInfo) {
        EasyResult<T> result = new EasyResult<>();
        result.setCode(code)
                .setMessage(message)
                .setErrorInfo(errorInfo);
        return result;
    }

    @JsonIgnore
    public static <T> EasyResult<T> error(Throwable e) {
        EasyResult<T> easyResult = new EasyResult<T>();
        easyResult.setCode(BusCode.A00003);
        easyResult.setMessage(I18nUtils.getOperateErrorStr());
        if (!(e instanceof EasyException)) {
            easyResult.setErrorInfo(SysLog.getStackTraceInfo(e));
        }
        return easyResult;
    }

    @JsonIgnore
    public static <T> EasyResult<T> errorGateway(Throwable e) {
        EasyResult<T> easyResult = new EasyResult<T>();
        easyResult.setCode(BusCode.A00061);
        easyResult.setMessage(e.getMessage());
        if (!(e instanceof EasyException)) {
            easyResult.setErrorInfo(SysLog.getStackTraceInfo(e));
        }
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
        String code = BusCode.A00003;
        // 不允许使用自己定义的内容发布异常
        if (msg.isEmpty()) {
            msg = isEasy4j ? e.getMessage() : I18nUtils.getMessage(code, local);
        } else {
            code = msgKey;
        }
        EasyResult<T> easyResult = new EasyResult<T>();
        easyResult.setMessage(msg);
        easyResult.setCode(code);
        if (!(e instanceof EasyException)) {
            easyResult.setErrorInfo(SysLog.getStackTraceInfo(e));
        }
        return easyResult;

    }


    @JsonIgnore
    public boolean isSuccess() {
        return String.valueOf(SysConstant.SUCCESS_CODE).equals(this.getCode());
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