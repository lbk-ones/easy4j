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

import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;

import java.util.List;

/**
 * OpDdlAlter
 * alter 从开始一直倒字段约束之间的部分
 * @author bokun.li
 * @date 2025/8/23
 */
public interface OpDdlAlter extends IOpContext,IOpMatch{

    String addColumn(DDLFieldInfo fieldInfo);

    String removeColumn(DDLFieldInfo fieldInfo);

    String renameColumnName(String oldName, String newColumnName);

    String renameConstraintName(String newConstraintName);

    String renameTableName(String newTableName);

    String setSchemaNewName(String schemaNewName);

    String setNewTableSpace(String newTableSpaceName);

    /**
     * 删除存在的表
     * @param tableName 表名称，不能夹杂其他名称
     * @param isExe 是否立即执行，false则代表只返回语句，true既执行语句也返回语句
     * @author bokun.li
     * @date 2025/9/12
     */
    String dropTableIfExists(String tableName, boolean isExe);

    /**
     * 清除所有的表，慎用
     *
     * @author bokun.li
     * @date 2025/9/12
     */
    List<String> dropALlTableIfExists(boolean isExe);

}
