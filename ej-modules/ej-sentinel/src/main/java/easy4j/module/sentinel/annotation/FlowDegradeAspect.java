/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.sentinel.annotation;

import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.google.common.collect.Maps;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Aspect for methods with {@link FlowDegradeResource} annotation.
 *
 * @author Eric Zhao
 */
@Aspect
public class FlowDegradeAspect extends FlowDegradeAspectSupport {
    private static final Map<String,String> CACHE_RULES = Maps.newConcurrentMap();

    @Pointcut("@annotation(easy4j.module.sentinel.annotation.FlowDegradeResource)")
    public void degradeResourceAnnotationPointcut() {
    }

    @Around("degradeResourceAnnotationPointcut()")
    public Object invokeResourceWithSentinel(ProceedingJoinPoint pjp) throws Throwable {
        Method originMethod = resolveMethod(pjp);

        FlowDegradeResource annotation = originMethod.getAnnotation(FlowDegradeResource.class);
        if (annotation == null) {
            // Should not go through here.
            throw new IllegalStateException("Wrong state for FlowDegrade annotation");
        }
        String resourceName = getResourceName(annotation.value(), originMethod);
        initRules(resourceName,annotation);
        EntryType entryType = annotation.entryType();
        int resourceType = annotation.resourceType();
        Entry entry = null;
        try {
            entry = SphU.entry(resourceName, resourceType, entryType, pjp.getArgs());
            return pjp.proceed();
        } catch (BlockException ex) {
            return handleBlockException(pjp, annotation, ex);
        } catch (Throwable ex) {
            Class<? extends Throwable>[] exceptionsToIgnore = annotation.exceptionsToIgnore();
            // The ignore list will be checked first.
            if (exceptionsToIgnore.length > 0 && exceptionBelongsTo(ex, exceptionsToIgnore)) {
                throw ex;
            }
            if (exceptionBelongsTo(ex, annotation.exceptionsToTrace())) {
                traceException(ex);
                return handleFallback(pjp, annotation, ex);
            }

            // No fallback function can handle the exception, so throw it out.
            throw ex;
        } finally {
            if (entry != null) {
                entry.exit(1, pjp.getArgs());
            }
        }
    }

    public static void initRules(String resourceName, FlowDegradeResource annotation) {
        if(StrUtil.isBlank(resourceName)){
            return;
        }
        String s = CACHE_RULES.get(resourceName);
        if(StrUtil.isNotBlank(s)){
            return;
        }
        CACHE_RULES.put(resourceName,"true");
        // 初始化流控规则
        List<FlowRule> flowRules = new ArrayList<>();
        FlowRule flowRule = new FlowRule();
        flowRule.setResource(resourceName); // 资源名称要与 @SentinelResource 中的 value 一致
        flowRule.setCount(annotation.flowCount()); // 每秒允许通过的请求数
        flowRule.setGrade(annotation.flowGrade()); // 限流模式为 QPS
        flowRules.add(flowRule);
        FlowRuleManager.loadRules(flowRules);

        // 初始化降级规则
        List<DegradeRule> degradeRules = new ArrayList<>();
        DegradeRule degradeRule = new DegradeRule();
        degradeRule.setResource(resourceName);
        degradeRule.setCount(annotation.degradeCount()); // 熔断阈值，这里表示异常比例阈值为 0.5
        degradeRule.setGrade(annotation.deGrade()); // 降级模式为异常比例
        degradeRule.setTimeWindow(annotation.timeWindow()); // 熔断时长，单位为秒
        degradeRules.add(degradeRule);
        DegradeRuleManager.loadRules(degradeRules);
    }
}
