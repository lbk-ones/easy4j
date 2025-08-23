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
package easy4j.infra.dbaccess.dynamic.dll.op.meta;

import lombok.Data;

/**
 * 数据库表元数据实体类
 * 存储数据库表的基本信息，包括表名、类型、注释等
 */
@Data
public class TableMetadata {

    /**
     * 表目录（可能为null）
     * 说明：数据库表所在的目录，不同数据库对目录的定义可能不同
     */
    private String tableCat;

    /**
     * 表模式（可能为null）
     * 说明：数据库表所在的模式（schema），用于表的逻辑分组管理
     */
    private String tableSchem;

    /**
     * 表名
     * 说明：数据库中表的名称
     */
    private String tableName;

    /**
     * 表类型
     * 说明：典型的类型包括"TABLE"（表）、"VIEW"（视图）、"SYSTEM TABLE"（系统表）、
     * "GLOBAL TEMPORARY"（全局临时表）、"LOCAL TEMPORARY"（本地临时表）、
     * "ALIAS"（别名）、"SYNONYM"（同义词）等
     */
    private String tableType;

    /**
     * 表的解释性注释（可能为null）
     * 说明：对表的业务含义或用途的描述说明
     */
    private String remarks;

    /**
     * 类型目录（可能为null）
     * 说明：表关联的类型所在的目录
     */
    private String typeCat;

    /**
     * 类型模式（可能为null）
     * 说明：表关联的类型所在的模式（schema）
     */
    private String typeSchem;

    /**
     * 类型名称（可能为null）
     * 说明：表关联的类型的名称
     */
    private String typeName;

    /**
     * 自引用列名（可能为null）
     * 说明：类型化表中指定的"标识符"列的名称，用于表的自关联
     */
    private String selfReferencingColName;

    /**
     * 引用生成方式（可能为null）
     * 说明：指定SELF_REFERENCING_COL_NAME列中的值如何生成，
     * 可能的值为"SYSTEM"（系统生成）、"USER"（用户生成）、"DERIVED"（派生生成）
     */
    private String refGeneration;
}
    