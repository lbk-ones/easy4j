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
package easy4j.infra.dbaccess.dynamic.dll.op.impl.tc;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;

import java.util.List;

/**
 *
 * @author bokun.li
 * @date 2025/8/23
 */
public class MysqlOpTableConstraints extends AbstractOpTableConstraints {

    public static final String DEFAULT_ENGINE = "InnoDB";
    public static final String DEFAULT_CHARSET = "utf8mb4";
    public static final String DEFAULT_CHARSET_COLLATE = "utf8mb4_general_ci";

    @Override
    public boolean match(OpContext opContext) {
        String dbType = opContext.getDbType();
        return DbType.MYSQL.getDb().equals(dbType);
    }

    @Override
    public List<String> getTableOptions() {
        OpContext opContext = getOpContext();
        CheckUtils.checkByLambda(opContext, OpContext::getDdlTableInfo);
        List<String> objects = ListTs.newList();
        DDLTableInfo ddlTableInfo = opContext.getDdlTableInfo();
        String comment = ddlTableInfo.getComment();
        objects.add("engine = " + StrUtil.blankToDefault(ddlTableInfo.getEngine(), DEFAULT_ENGINE));
        objects.add("default charset = " + StrUtil.blankToDefault(ddlTableInfo.getCharset(), DEFAULT_CHARSET));
        objects.add("collate = " + StrUtil.blankToDefault(ddlTableInfo.getCollate(), DEFAULT_CHARSET_COLLATE));
        if(StrUtil.isNotBlank(comment)){
            objects.add("comment '"+comment+"'");
        }
        return objects;
    }
}
