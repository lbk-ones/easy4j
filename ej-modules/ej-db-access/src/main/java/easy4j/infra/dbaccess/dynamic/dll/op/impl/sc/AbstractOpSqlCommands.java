package easy4j.infra.dbaccess.dynamic.dll.op.impl.sc;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;
import easy4j.infra.dbaccess.dynamic.dll.op.api.OpSqlCommands;
import easy4j.infra.dbaccess.helper.DDlHelper;
import lombok.Getter;

import java.io.IOException;

@Getter
public abstract class AbstractOpSqlCommands implements OpSqlCommands {

    OpContext opContext;

    @Override
    public boolean match(OpContext opContext) {
        return true;
    }

    @Override
    public void setOpContext(OpContext opContext) {
        this.opContext = opContext;
    }

    @Override
    public void exeDDLStr(String segment) {
        String ddl = StrUtil.trim(segment);
        if(StrUtil.isBlank(segment)) return;
        if (!ddl.endsWith(SP.SEMICOLON)) {
            ddl = ddl + SP.SEMICOLON;
        }
        if (StrUtil.isNotBlank(ddl)) {
            try {
                DDlHelper.execDDL(getOpContext().getConnection(), ddl, null, true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
