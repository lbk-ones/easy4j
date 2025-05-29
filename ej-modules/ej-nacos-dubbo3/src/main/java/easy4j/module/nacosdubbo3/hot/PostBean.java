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
package easy4j.module.nacosdubbo3.hot;

import cn.hutool.core.util.ReflectUtil;
import easy4j.module.base.utils.ListTs;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * PostBean
 *
 * @author bokun.li
 * @date 2025-05
 */
public class PostBean implements BeanPostProcessor {


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        collector(bean,beanName);
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    public void collector(Object bean, String beanName){
        Class<?> aClass = bean.getClass();
        String name = aClass.getName();
        List<String> list = ListTs.asList("com.alibaba", "org.springframework", "org.apache", "org.mybatis", "com.baomidou.mybatisplus");
        if (list.stream().noneMatch(name::startsWith)) {
            Field[] fields = ReflectUtil.getFields(aClass);
            for (Field field : fields) {
                if (field.isAnnotationPresent(Value.class)) {
                    System.out.println("==========================="+beanName+"============="+bean.toString());
                }
            }
            Method[] methods = ReflectUtil.getMethods(aClass);
            for (Method method : methods) {
                if (method.isAnnotationPresent(Value.class)) {
                    System.out.println("==========================="+beanName+"============="+bean.toString());
                }
            }
        }
    }
}
