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
package easy4j.module.jpa.aware;

import easy4j.module.base.annotations.Desc;

import java.util.Optional;

/**
 * EasyJpaAuditorAware
 *
 * @author bokun.li
 * @date 2025-05
 */
@Desc("jpa自动审计 实现接口 并注入 bean EasyJpaAuditorAware 不能再去实现  AuditorAware 接口 不然会有问题")
public interface EasyJpaAuditorAware {

    Optional<String> getCurrentAuditor();

}
