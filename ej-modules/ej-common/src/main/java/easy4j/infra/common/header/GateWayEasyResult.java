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
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.common.utils.json.JacksonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 统一返回消息体
 *
 * @param <T>
 * @author bokun.li
 */
@Setter
@Getter
@Schema(description = "网关通用信息返回实体", name = "GateWayEasyResult")
public class GateWayEasyResult<T> implements Serializable {

    private static final long serialVersionUID = 6044312338316185330L;

    // 业务状态码
    @Schema(description = "业务状态码（0=成功，非0=错误）")
    private String code;

    // 提示消息
    @Schema(description = "提示消息")
    private String message;

    // 错误堆栈信息
    @Schema(description = "错误堆栈信息")
    private String errorInfo;
    // 返回对象
    @Schema(description = "返回对象")
    private T data;


    public static <T> GateWayEasyResult<T> ok(T data) {

        GateWayEasyResult<T> easyResult = new GateWayEasyResult<T>();

        easyResult.setData(data);
        return easyResult;
    }

    public static <T> GateWayEasyResult<T> ok(T data, String message) {
        GateWayEasyResult<T> easyResult = new GateWayEasyResult<T>();
        easyResult.setData(data);
        easyResult.setMessage(message);
        return easyResult;
    }

    @JsonIgnore
    public static <T> GateWayEasyResult<T> error(String message) {
        GateWayEasyResult<T> easyResult = new GateWayEasyResult<T>();
        easyResult.setCode(String.valueOf(SysConstant.ERRORCODE));
        easyResult.setMessage(message);
        easyResult.setData(null);
        return easyResult;
    }


    // 失败返回（code ≠ 0）
    public static <T> GateWayEasyResult<T> error(String code, String message) {
        GateWayEasyResult<T> result = new GateWayEasyResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    // 失败返回（带异常信息）
    public static <T> GateWayEasyResult<T> error(String code, String message, String errorInfo) {
        GateWayEasyResult<T> result = new GateWayEasyResult<>();
        result.setCode(code);
        result.setMessage(message);
        result.setErrorInfo(errorInfo);
        return result;
    }

    @JsonIgnore
    public static <T> GateWayEasyResult<T> errorGateway(Throwable e) {
        GateWayEasyResult<T> easyResult = new GateWayEasyResult<T>();
        easyResult.setCode(BusCode.A00061);
        //easyResult.setError(SysConstant.ERRORCODE);
        easyResult.setMessage(e.getMessage());
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
    public static <T> GateWayEasyResult<T> rpcErrorInfo(Exception e) {
        GateWayEasyResult<T> easyResult = new GateWayEasyResult<>();
        easyResult.setCode("A00003");
        String message1 = e.getMessage();
        easyResult.setMessage(message1);
        easyResult.setErrorInfo("");
        easyResult.setData(null);
        return easyResult;
    }


    public GateWayEasyResult() {
        this.code = "0"; // 默认成功
        this.message = "gateway";
        //setError(SysConstant.SUCCESSCODE);
        //setCode("A00001");
        //setMessage(I18nBean.getOperateSuccessStr());
    }


    @JsonIgnore
    public boolean isSuccess() {
        return "0".equals(code);
        //return error == 0;
    }

    public GateWayEasyResult(int error, String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /*public EasyResult(int error) {
        this.error = error;
    }*/
    public GateWayEasyResult(int error) {
        this.code = String.valueOf(error);
    }

    public GateWayEasyResult(String code) {
        this.code = code;
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
