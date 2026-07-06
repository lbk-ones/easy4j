/*
 * Copyright 2013-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.openfeign;

import io.github.lbkones.cloud.openfeign.Easy4jOpenFeignFallbackFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.cloud.context.named.NamedContextFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * A factory that creates instances of feign classes. It creates a Spring
 * ApplicationContext per client name, and extracts the beans that it needs from there.
 *
 * @author Spencer Gibb
 * @author Dave Syer
 * @author Matt King
 * @author Jasbir Singh
 * @author Olga Maciaszek-Sharma
 */
public class FeignClientFactory extends NamedContextFactory<FeignClientSpecification> {

	public FeignClientFactory() {
		this(new HashMap<>());
	}

	public FeignClientFactory(
			Map<String, ApplicationContextInitializer<GenericApplicationContext>> applicationContextInitializers) {
		super(FeignClientsConfiguration.class, "spring.cloud.openfeign", "spring.cloud.openfeign.client.name",
				applicationContextInitializers);
	}

	@Nullable
	public <T> T getInstanceWithoutAncestors(String name, Class<T> type) {
		try {
			return BeanFactoryUtils.beanOfType(getContext(name), type);
		}
		catch (BeansException ex) {
			return null;
		}
	}

	@Nullable
	public <T> Map<String, T> getInstancesWithoutAncestors(String name, Class<T> type) {
		return getContext(name).getBeansOfType(type);
	}

	public <T> T getInstance(String contextName, String beanName, Class<T> type) {
		return getContext(contextName).getBean(beanName, type);
	}

	public GenericApplicationContext getContext(String name){
		return super.getContext(name);
	}

	/**
	 * 从指定上下文拿取类型为type的bean 但是有点治标不治本 也是没有办法中的办法
	 * 重写下这个方法 找不到再根据名称找一下
	 * @param name 名称
	 * @param type 类型
	 * @return 返回bean
	 * @param <T> 泛型
	 */
	@Override
	public <T> T getInstance(String name, Class<T> type) {
		GenericApplicationContext context = super.getContext(name);

		if(type == Easy4jOpenFeignFallbackFactory.class){
			try{
				Object bean = context.getBean(name);
				if(bean != null && type.isAssignableFrom(bean.getClass())){
					return (T) bean;
				}
			}catch (NoSuchBeanDefinitionException e2){
				// ignore
				return context.getBean(type);
			}
		}
		try {
			return context.getBean(type);
		}
		catch (NoSuchBeanDefinitionException e) {
			// ignore
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public FeignClientFactory withApplicationContextInitializers(Map<String, Object> applicationContextInitializers) {
		Map<String, ApplicationContextInitializer<GenericApplicationContext>> convertedInitializers = new HashMap<>();
		applicationContextInitializers.keySet()
			.forEach(contextId -> convertedInitializers.put(contextId,
					(ApplicationContextInitializer<GenericApplicationContext>) applicationContextInitializers
						.get(contextId)));
		return new FeignClientFactory(convertedInitializers);
	}

}
