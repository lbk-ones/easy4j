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
package easy4j.infra.dbaccess.dynamic.dll.op;

import cn.hutool.core.util.ReflectUtil;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.dynamic.dll.op.api.*;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.al.MysqlOpDdlAlter;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.al.OracleOpDdlAlter;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.al.PgOpDdlAlter;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.al.SqlServerOpDdlAlter;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.cc.MysqlOpColumnConstraints;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.cc.OracleOpColumnConstraints;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.cc.PgOpColumnConstraints;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.cc.SqlServerOpColumnConstraints;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.ct.MysqlOpDdlCreateTable;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.ct.OracleOpDdlCreateTable;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.ct.PgOpDdlCreateTable;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.ct.SqlServerOpDdlCreateTable;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.idx.CommonOpDdlIndexImpl;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.sc.OpSqlCommandsImpl;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.tc.MysqlOpTableConstraints;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.tc.OracleOpTableConstraints;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.tc.PgOpTableConstraints;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.tc.SqlServerOpTableConstraints;

import java.util.Comparator;
import java.util.List;

/**
 * OpSelector
 * 选择器
 *
 * @author bokun.li
 * @date 2025/8/23
 */
public class OpSelector {

    private static final List<OpColumnConstraints> columnConstraintsList = ListTs.newLinkedList();
    private static final List<OpTableConstraints> tableConstraintsList = ListTs.newLinkedList();
    private static final List<OpDdlCreateTable> createTableList = ListTs.newLinkedList();
    private static final List<OpSqlCommands> opSqlCommandsList = ListTs.newLinkedList();
    private static final List<OpDdlIndex> opDdlIndicesList = ListTs.newLinkedList();
    private static final List<OpDdlAlter> opDdlAlterList = ListTs.newLinkedList();

    static {
        // column constraints
        columnConstraintsList.add(new MysqlOpColumnConstraints());
        columnConstraintsList.add(new OracleOpColumnConstraints());
        columnConstraintsList.add(new PgOpColumnConstraints());
        columnConstraintsList.add(new SqlServerOpColumnConstraints());
        columnConstraintsList.sort(Comparator.comparingInt(IOpContext::getSort));

        // table constraints
        tableConstraintsList.add(new MysqlOpTableConstraints());
        tableConstraintsList.add(new OracleOpTableConstraints());
        tableConstraintsList.add(new PgOpTableConstraints());
        tableConstraintsList.add(new SqlServerOpTableConstraints());
        tableConstraintsList.sort(Comparator.comparingInt(IOpContext::getSort));

        // create table
        createTableList.add(new MysqlOpDdlCreateTable());
        createTableList.add(new OracleOpDdlCreateTable());
        createTableList.add(new PgOpDdlCreateTable());
        createTableList.add(new SqlServerOpDdlCreateTable());
        createTableList.sort(Comparator.comparingInt(IOpContext::getSort));


        opSqlCommandsList.add(new OpSqlCommandsImpl());
        opSqlCommandsList.sort(Comparator.comparingInt(IOpContext::getSort));

        opDdlIndicesList.add(new CommonOpDdlIndexImpl());
        opDdlIndicesList.sort(Comparator.comparingInt(IOpContext::getSort));


        opDdlAlterList.add(new MysqlOpDdlAlter());
        opDdlAlterList.add(new OracleOpDdlAlter());
        opDdlAlterList.add(new PgOpDdlAlter());
        opDdlAlterList.add(new SqlServerOpDdlAlter());
        opDdlAlterList.sort(Comparator.comparingInt(IOpContext::getSort));
    }

    /**
     * 选择列约束
     *
     * @param opContext
     * @return
     */
    public static OpColumnConstraints selectOpCC(OpContext opContext) {
        CheckUtils.notNull(opContext, "opContext");
        for (OpColumnConstraints opColumnConstraints : columnConstraintsList) {
            if (opColumnConstraints.match(opContext)) {
                return newInstance(opColumnConstraints.getClass(), opContext);
            }
        }
        throw EasyException.wrap(BusCode.A00047, "opContext");
    }

    /**
     * 选择表约束
     *
     * @param opContext
     * @return
     */
    public static OpTableConstraints selectOpCT(OpContext opContext) {
        CheckUtils.notNull(opContext, "opContext");
        for (OpTableConstraints opColumnConstraints : tableConstraintsList) {
            if (opColumnConstraints.match(opContext)) {
                return newInstance(opColumnConstraints.getClass(), opContext);
            }
        }
        throw EasyException.wrap(BusCode.A00047, "opContext");
    }

    private static <T extends IOpContext> T newInstance(Class<T> o, OpContext opContext) {
        T t = ReflectUtil.newInstance(o);
        t.setOpContext(opContext);
        return t;
    }

    /**
     * 选择建表器
     *
     * @param opContext
     * @return
     */
    public static OpDdlCreateTable selectOpCreateTable(OpContext opContext) {
        CheckUtils.notNull(opContext, "opContext");
        for (OpDdlCreateTable opCreateTableRule : createTableList) {
            if (opCreateTableRule.match(opContext)) {
                return newInstance(opCreateTableRule.getClass(), opContext);
            }
        }
        throw EasyException.wrap(BusCode.A00047, "opContext");
    }

    /**
     * 获取sql命令执行器
     *
     * @param opContext
     * @return
     */
    public static OpSqlCommands selectOpSqlCommands(OpContext opContext) {
        CheckUtils.notNull(opContext, "opContext");
        for (OpSqlCommands opCreateTableRule : opSqlCommandsList) {
            if (opCreateTableRule.match(opContext)) {
                return newInstance(opCreateTableRule.getClass(), opContext);
            }
        }
        throw EasyException.wrap(BusCode.A00047, "opContext");
    }

    /**
     * 获取索引实现
     *
     * @param opContext
     * @return
     */
    public static OpDdlIndex selectOpIndex(OpContext opContext) {
        CheckUtils.notNull(opContext, "opContext");
        for (OpDdlIndex opCreateTableRule : opDdlIndicesList) {
            if (opCreateTableRule.match(opContext)) {
                return newInstance(opCreateTableRule.getClass(), opContext);

            }
        }
        throw EasyException.wrap(BusCode.A00047, "opContext");
    }

    /**
     * 获取alter实现
     *
     * @param opContext
     * @return
     */
    public static OpDdlAlter selectOpDdlAlter(OpContext opContext) {
        CheckUtils.notNull(opContext, "opContext");
        for (OpDdlAlter opCreateTableRule : opDdlAlterList) {
            if (opCreateTableRule.match(opContext)) {
                return newInstance(opCreateTableRule.getClass(), opContext);
            }
        }
        throw EasyException.wrap(BusCode.A00047, "opContext");
    }

}
