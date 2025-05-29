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
package easy4j.module.sentinel;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import easy4j.module.base.header.EasyResult;
import easy4j.module.base.utils.BusCode;
import lombok.extern.slf4j.Slf4j;

/**
 * GlobalFallbackHandler
 *
 * @author bokun.li
 * @date 2025-05
 */
@Slf4j
public class GlobalFallbackHandler {

    public static EasyResult defaultFallback(Throwable throwable) {
        if(!(throwable instanceof BlockException)){
            return EasyResult.toI18n(throwable);
        }else{
            BlockException ex = (BlockException) throwable;
            String resourceName = ex.getRule().getResource();
            String errorMsg = String.format("资源 [%s] 触发限流规则: %s", resourceName, ex.getClass().getSimpleName());
            log.error(errorMsg);
            if (ex instanceof FlowException) {
                errorMsg = BusCode.A00022;
            } else if (ex instanceof DegradeException) {
                errorMsg = BusCode.A00023;
            }
            return EasyResult.parseFromI18n(1,errorMsg);
        }

    }  
}  
