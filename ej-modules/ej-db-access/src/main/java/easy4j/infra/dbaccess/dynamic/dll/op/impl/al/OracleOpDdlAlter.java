package easy4j.infra.dbaccess.dynamic.dll.op.impl.al;

import easy4j.infra.common.enums.DbType;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;


public class OracleOpDdlAlter extends AbstractOpDdlAlter {

    @Override
    public boolean match(OpContext opContext) {
        String dbType = opContext.getDbType();
        return DbType.ORACLE.getDb().equals(dbType);
    }
}
