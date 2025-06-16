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
package easy4j.infra.webmvc;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.exception.ExceptionList;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.i18n.I18nUtils;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.api.dblog.Easy4jDbLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 捕捉到Exception、EasyException异常，会由此对象拦截处理
 *
 * @author bokun.li
 * @date 2023/11/23
 */
@ControllerAdvice
@Slf4j
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GlobalExceptionHandler {

    @Autowired
    Easy4jContext easy4jContext;


    @ExceptionHandler(value = EasyException.class)
    @ResponseBody
    public EasyResult<Object> appErrorHandler(HttpServletRequest req, EasyException e) throws Exception {
        log.info("运行时自定义异常-------" + e.getMessage());
        return EasyResult.toI18n(e);
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public EasyResult<Object> defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        log.error("不可预期的异常：" + e.getMessage(), e);
        if (e instanceof EasyException) {
            return EasyResult.rpcErrorInfo(e);
        } else {
            // fix: unexpected exception cannot be recorded to db
            try {
                Easy4jDbLog easy4jDbLog = easy4jContext.get(Easy4jDbLog.class);
                easy4jDbLog.setException(e);
            } catch (Exception e2) {
                Easy4j.info(SysLog.compact("context get has a error:" + e2.getMessage()));
                e.printStackTrace();
            }
            return EasyResult.errorInfo(e);
        }
    }


//    @ExceptionHandler(value = AspectValidateException.class)
//    @ResponseBody
//    public Map<String, Object> aspectValidateExceptionHandler(HttpServletRequest req, AspectValidateException e) throws Exception {
//        log.info("验证数据时产生异常-------"+e.getMessage());
//        return new RestResult().createResult("", e.getMessage(), e.getError());
//    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    public EasyResult<Object> methodArgumentNotValidExceptionHandler(HttpServletRequest req, MethodArgumentNotValidException e) throws Exception {
        log.info("Controller验证数据时产生异常-------" + e.getMessage());
        ExceptionList<String> list = new ExceptionList<String>();
        for (ObjectError o : e.getBindingResult().getAllErrors()) {
            String fieldValue = Convert.toStr(ReflectUtil.getFieldValue(o, "field"), "");
            list.add((StrUtil.isNotBlank(fieldValue) ? "【" + fieldValue + "】" : "") + o.getDefaultMessage());
        }
        try {
            Easy4jDbLog easy4jDbLog = easy4jContext.get(Easy4jDbLog.class);
            String message = e.getMessage();
            RuntimeException wrapException = new RuntimeException(message);
            easy4jDbLog.setException(wrapException);
        } catch (Exception e2) {
            Easy4j.info(SysLog.compact("context get has a error:" + e2.getMessage()));
            e.printStackTrace();
        }
        return EasyResult.errorInfo(list.toString());
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    @ResponseBody
    public EasyResult<Object> httpErrorHandler(HttpServletRequest req, HttpMessageNotReadableException e) throws Exception {
        log.info("HTTP请求参数异常-------" + e.getMessage());
        return EasyResult.errorInfo(I18nUtils.getMessage(BusCode.A00005, e.getMessage()));
    }

    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    @ResponseBody
    public EasyResult<Object> httpMediaTypeNotSupportedExceptionHandler(HttpServletRequest req, HttpMediaTypeNotSupportedException e) throws Exception {
        log.info("HTTP请求ContentType异常-------" + e.getMessage());
        return EasyResult.errorInfo(I18nUtils.getMessage(BusCode.A00006));
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public EasyResult<Object> httpMethodErrorHandler(HttpServletRequest req, HttpRequestMethodNotSupportedException e) throws Exception {
        return EasyResult.errorInfo(I18nUtils.getMessage(BusCode.A00007, e.getMessage()));
    }
}

