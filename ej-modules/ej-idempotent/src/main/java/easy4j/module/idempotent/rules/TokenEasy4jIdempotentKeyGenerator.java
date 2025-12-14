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
package easy4j.module.idempotent.rules;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.context.api.idempotent.Easy4jIdempotentKeyGenerator;
import easy4j.infra.common.utils.SysConstant;

import javax.servlet.http.HttpServletRequest;

/**
 * FormEasy4jIdempotentKeyGenerator
 * 以token为条件来进行判断
 *
 * @author bokun.li
 * @date 2025-05
 */
public class TokenEasy4jIdempotentKeyGenerator implements Easy4jIdempotentKeyGenerator {

    @Override
    public String generate(HttpServletRequest request) {
        String headerToken = request.getHeader(SysConstant.X_ACCESS_TOKEN);
        String formToken = request.getParameter(SysConstant.X_ACCESS_TOKEN);
        if (
                StrUtil.isBlank(headerToken) ||
                        StrUtil.isBlank(formToken)
        ) {
            return "";
        }
        return StrUtil.blankToDefault(
                headerToken,
                formToken
        ) + getUri(request);
    }
}
