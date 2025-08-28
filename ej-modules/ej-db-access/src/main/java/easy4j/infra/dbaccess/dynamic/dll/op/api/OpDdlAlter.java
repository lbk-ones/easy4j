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

/**
 * OpDdlAlter
 * alter 从开始一直倒字段约束之间的部分
 * @author bokun.li
 * @date 2025/8/23
 */
public interface OpDdlAlter extends IOpContext,IOpMatch{

    String getAddColumnSegment(DDLFieldInfo fieldInfo);

    String getRemoveColumnSegment(DDLFieldInfo fieldInfo);

    String getRenameColumnNameSegment(String oldName, String newColumnName);

    String getRenameConstraintNameSegment(String newConstraintName);

    String getRenameTableNameSegment(String newTableName);

    String getSetSchemaNewNameSegment(String schemaNewName);

    String getSetNewTableSpaceSegment(String newTableSpaceName);

}
