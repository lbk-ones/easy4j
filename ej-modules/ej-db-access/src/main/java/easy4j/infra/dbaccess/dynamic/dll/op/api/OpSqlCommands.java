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

import easy4j.infra.dbaccess.condition.WhereBuild;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.sc.CopyDbConfig;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * OpSqlCommands
 * 杂项 不知道放在哪个模块就放这里来
 *
 * @author bokun.li
 * @date 2025/8/23
 */
public interface OpSqlCommands extends IOpContext, IOpMatch {


    /**
     * 执行ddl语句
     *
     * @param segment
     */
    void exeDDLStr(String segment);

    /**
     * 指定数据源执行ddl语句
     * 不会自动关闭连接
     *
     * @param connection        指定的连接
     * @param segment           sql语句
     * @param isCloseConnection 是否关闭连接
     */
    void exeDDLStr(Connection connection, String segment, boolean isCloseConnection);

    /**
     * 指定表名称，然后将传入得dict中得键值对组装好，写入表并返回自增字段
     *
     * @param dict
     */
    Map<String, Object> dynamicSave(Map<String, Object> dict);

    /**
     * 动态字段更新
     * 指定表名称，然后将传入得dict中得键值对组装好，根据传入的条件更新表
     *
     * @param dict
     */
    int dynamicUpdate(Map<String, Object> dict, boolean updateNull, WhereBuild whereBuild);


    /**
     * 通过java Class 自动执行ddl语句 没有就建表，有就检测要新增得字段，只新增不修改
     *
     * @param isExe 是否执行
     * @author bokun.li
     * @date 2025/9/1
     */
    String autoDDLByJavaClass(boolean isExe);


    /**
     * 批量解析整个数据源得ddl语句，根据条件来
     *
     * @param tablePrefix  表得前缀或者全名前缀一般是 xxx_%（前缀为xxx_）
     * @param tableType    TABLE / VIEW null默认TABLE
     * @param copyDbConfig 要生成的数据库类型如果为null 那么代表是当前数据库类型，（当前数据库就是构造方法传入的那个dataSource所代表的数据库类型）
     * @return
     */
    List<String> copyDataSourceDDL(String[] tablePrefix, @Nullable String[] tableType, @Nullable CopyDbConfig copyDbConfig);

}
