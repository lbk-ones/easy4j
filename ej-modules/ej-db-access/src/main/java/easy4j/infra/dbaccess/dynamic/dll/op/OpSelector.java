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

import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.dynamic.dll.op.api.IOpContext;
import easy4j.infra.dbaccess.dynamic.dll.op.api.OpColumnConstraints;
import easy4j.infra.dbaccess.dynamic.dll.op.api.OpDdlCreateTable;
import easy4j.infra.dbaccess.dynamic.dll.op.api.OpTableConstraints;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.cc.MysqlOpColumnConstraints;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.cc.OracleOpColumnConstraints;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.cc.PgOpColumnConstraints;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.cc.SqlServerOpColumnConstraints;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.ct.MysqlOpDdlCreateTable;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.ct.OracleOpDdlCreateTable;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.ct.PgOpDdlCreateTable;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.ct.SqlServerOpDdlCreateTable;
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
    }

    /**
     * 选择列约束
     *
     * @param opContext
     * @return
     */
    public static OpColumnConstraints selectOpCC(OpContext opContext) {
        for (OpColumnConstraints opColumnConstraints : columnConstraintsList) {
            opColumnConstraints.setOpContext(opContext);
            if (opColumnConstraints.match(opContext)) {
                return opColumnConstraints;
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
        for (OpTableConstraints opColumnConstraints : tableConstraintsList) {
            if (opColumnConstraints.match(opContext)) {
                return opColumnConstraints;
            }
        }
        throw EasyException.wrap(BusCode.A00047, "opContext");
    }

    /**
     * 选择建表器
     *
     * @param opContext
     * @return
     */
    public static OpDdlCreateTable selectOpCreateTable(OpContext opContext) {
        for (OpDdlCreateTable opColumnConstraints : createTableList) {
            if (opColumnConstraints.match(opContext)) {
                return opColumnConstraints;
            }
        }
        throw EasyException.wrap(BusCode.A00047, "opContext");
    }

}
