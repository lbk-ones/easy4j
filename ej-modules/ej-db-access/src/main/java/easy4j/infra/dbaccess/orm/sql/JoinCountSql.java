package easy4j.infra.dbaccess.orm.sql;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.orm.OperateType;
import easy4j.infra.dbaccess.orm.RuntimeContext;

public class JoinCountSql extends JoinSql {

    @Override
    public <T> boolean match(RuntimeContext<T> runtimeContext) {
        return runtimeContext.getOperateType() == OperateType.SELECT_JOIN_COUNT;
    }

    @Override
    public <T> String build(RuntimeContext<T> runtimeContext) {
        String s = "_e" + RandomUtil.randomString(3);
        String build = super.build(runtimeContext);
        String lastSql = runtimeContext.getLastSql();
        if (StrUtil.isNotBlank(lastSql)) {
            build += SP.SPACE + lastSql;
        }
        return "select count(1) from (" +
                build + ") " + s;
    }
}
