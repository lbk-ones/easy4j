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
package easy4j.module.sca.config;

import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;

/**
 * CustomPropertyResolver
 * 直接修改底层的值 暂时不用
 *
 * @author bokun.li
 * @date 2025-06-14 11:14:55
 */
@Deprecated
public class CustomPropertyResolver extends PropertySourcesPropertyResolver {
    private final MutablePropertySources propertySources;

    public CustomPropertyResolver(MutablePropertySources propertySources) {
        super(propertySources);
        this.propertySources = propertySources;
    }

    public void updateProperty(String type, String name, Object value) {
        propertySources.stream()
                .filter(ps -> ps.containsProperty(name) && ps.getName().equals(type))
                .findFirst()
                .ifPresent(ps -> {
                    if (ps instanceof MapPropertySource) {
                        ((MapPropertySource) ps).getSource().put(name, value);
                    }
                });
    }
}