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
package easy4j.module.sca.handler;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.header.EasyResult;
import easy4j.module.base.utils.BusCode;
import easy4j.module.base.utils.SysConstant;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ej-sentinel 模块没有  BlockExceptionHandler 这个东东 所以那里的逻辑不动
 * 全局Sentinel自定义信息处理
 */
public class CustomSentinelExceptionHandler implements BlockExceptionHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException ex) throws Exception {

        String msg = null;

        if (ex instanceof FlowException) {
            msg = BusCode.A00022;

        } else if (ex instanceof DegradeException) {
            msg = BusCode.A00024;

        } else if (ex instanceof ParamFlowException) {
            msg = BusCode.A00025;

        } else if (ex instanceof SystemBlockException) {
            msg = BusCode.A00026;

        } else if (ex instanceof AuthorityException) {
            msg = BusCode.A00027;

        } else {
            msg = BusCode.A00028;
        }
        response.setStatus(200);
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Type", "application/json;charset=utf-8");
        response.setContentType("application/json;charset=utf-8");
        EasyResult<Object> result = EasyResult.parseFromI18n(500, msg);
        response.getWriter().write(result.toString());
    }

}
