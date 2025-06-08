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
package easy4j.module.sentinel.annotation;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.common.exception.EasyException;
import easy4j.module.sentinel.GlobalFallbackHandler;

import java.lang.annotation.*;

/**
 * FlowDegradeResource
 *
 * @author bokun.li
 * @date 2025-05
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface FlowDegradeResource {

    /**
     * @return name of the Sentinel resource
     */
    String value() default "";

    /**
     * @return the entry type (inbound or outbound), outbound by default
     */
    EntryType entryType() default EntryType.OUT;

    /**
     * @return the classification (type) of the resource
     * @since 1.7.0
     */
    int resourceType() default 0;

    /**
     * @return name of the block exception function, empty by default
     */
    String blockHandler() default "";

    /**
     * The {@code blockHandler} is located in the same class with the original method by default.
     * However, if some methods share the same signature and intend to set the same block handler,
     * then users can set the class where the block handler exists. Note that the block handler method
     * must be static.
     *
     * @return the class where the block handler exists, should not provide more than one classes
     */
    Class<?>[] blockHandlerClass() default {};

    /**
     * @return name of the fallback function, empty by default
     */
    String fallback() default "";

    /**
     * The {@code defaultFallback} is used as the default universal fallback method.
     * It should not accept any parameters, and the return type should be compatible
     * with the original method.
     *
     * @return name of the default fallback method, empty by default
     * @since 1.6.0
     */
    String defaultFallback() default "defaultFallback";

    /**
     * The {@code fallback} is located in the same class with the original method by default.
     * However, if some methods share the same signature and intend to set the same fallback,
     * then users can set the class where the fallback function exists. Note that the shared fallback method
     * must be static.
     *
     * @return the class where the fallback method is located (only single class)
     * @since 1.6.0
     */
    Class<?>[] fallbackClass() default {GlobalFallbackHandler.class};

    /**
     * @return the list of exception classes to trace, {@link Throwable} by default
     * @since 1.5.1
     */
    Class<? extends Throwable>[] exceptionsToTrace() default {Throwable.class};

    /**
     * Indicates the exceptions to be ignored. Note that {@code exceptionsToTrace} should
     * not appear with {@code exceptionsToIgnore} at the same time, or {@code exceptionsToIgnore}
     * will be of higher precedence.
     *
     * @return the list of exception classes to ignore, empty by default
     * @since 1.6.0
     */
    Class<? extends Throwable>[] exceptionsToIgnore() default {EasyException.class};

    @Desc("流控模式 0 代表线程数限流(RuleConstant.FLOW_GRADE_THREAD) 1代表qps(RuleConstant.FLOW_GRADE_QPS)")
    // 流控模式
    int flowGrade() default RuleConstant.FLOW_GRADE_QPS;

    @Desc("限流数量 默认100")
    // 流控数量
    int flowCount() default 100;

    @Desc("降级模式 默认为 异常比例")
    int deGrade() default RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO;

    @Desc("降级阈值 默认为 0.5")
    double degradeCount() default 0.5;

    @Desc("熔断时间 默认10秒")
    int timeWindow() default 10;


}
