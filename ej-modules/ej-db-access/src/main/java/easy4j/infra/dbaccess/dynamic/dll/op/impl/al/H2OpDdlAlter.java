package easy4j.infra.dbaccess.dynamic.dll.op.impl.al;

import easy4j.infra.common.enums.DbType;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;


public class H2OpDdlAlter extends AbstractOpDdlAlter {

    @Override
    public boolean match(OpContext opContext) {
        String dbType = opContext.getDbType();
        return DbType.H2.getDb().equals(dbType);
    }
}
