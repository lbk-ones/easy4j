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
package easy4j.infra.dbaccess.dynamic.dll.op.api;

import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;

import java.util.List;

/**
 * OpTableConstraints
 * 表级约束
 *
 * @author bokun.li
 * @date 2025/8/23
 */
public interface OpTableConstraints extends IOpContext {

    boolean match(OpContext opContext);

    /**
     * 获取表约束
     *
     * @return
     */
    List<String> getTableConstraints();

    /**
     * 获取表的属性 通常来说 是在 create table xx() 右括号后面的属性
     *
     * @return
     */
    List<String> getTableOptions();

    /**
     * 分区表相关
     * @return
     */
    List<String> getPartitionOptions();


}
