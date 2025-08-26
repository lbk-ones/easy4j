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
/**
 * OpSqlCommands
 * 杂项 不知道放在哪个模块就放这里来
 * @author bokun.li
 * @date 2025/8/23
 */
public interface OpSqlCommands extends IOpContext,IOpMatch {


    /**
     * 执行ddl语句
     * @param segment
     */
    void exeDDLStr(String segment);

}
