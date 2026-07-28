package easy4j.infra.dbaccess.orm.sql;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.orm.AccessField;
import easy4j.infra.dbaccess.orm.AccessUtils;
import easy4j.infra.dbaccess.orm.OperateType;
import easy4j.infra.dbaccess.orm.RuntimeContext;
import easy4j.infra.dbaccess.orm.sql.dialect.ISqlDialect;
import easy4j.infra.dbaccess.orm.sql.dialect.SqlDialectFactory;

import java.util.List;
import java.util.Objects;

// select * from table where xxx
public class QuerySql extends AbsISql {

    @Override
    public <T> boolean match(RuntimeContext<T> runtimeContext) {
        return runtimeContext.getOperateType() == OperateType.SELECT;
    }

    @Override
    public <T> String build(RuntimeContext<T> runtimeContext) {
        StringBuilder TEMP = new StringBuilder("select");
        List<String> selectFields = runtimeContext.getEscapeSelectFields();
        // 1
        if (CollUtil.isNotEmpty(selectFields)) {
            TEMP.append(SP.SPACE).append(ListTs.join(SP.DOT, selectFields));
        } else {
            List<AccessField> columnInfoList = runtimeContext.getColumnInfoList();
            if (columnInfoList.stream().anyMatch(e-> StrUtil.isNotBlank(e.getAlias()))) {
                int k = 0;
                for (AccessField accessField : columnInfoList) {
                    TEMP.append(SP.SPACE);
                    if(k!=0){
                        TEMP.append(SP.COMMA);
                    }
                    TEMP.append(StrUtil.blankToDefault(accessField.getAlias(), accessField.getEscapeColumnName()));
                    k++;
                }
            }else{
                TEMP.append(SP.SPACE).append("*");
            }

        }
        TEMP.append(" from ");

        // 2
        TEMP.append(runtimeContext.getDotTableName());

        // 3
        String whereSql = runtimeContext.getWhereSql();

        TEMP = new StringBuilder(runtimeContext.getAccessUtils().appendWhere(TEMP.toString(), whereSql));

        String lastSql = runtimeContext.getLastSql();
        if (StrUtil.isNotBlank(lastSql)) {
            TEMP.append(SP.SPACE).append(lastSql);
        }
        return TEMP.toString().trim();
    }
}
