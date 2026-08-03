package easy4j.infra.dbaccess.dll.op.impl.al;

import easy4j.infra.common.enums.DbType;
import easy4j.infra.dbaccess.dll.op.OpContext;


public class Db2OpDdlAlter extends AbstractOpDdlAlter {

    @Override
    public boolean match(OpContext opContext) {
        String dbType = opContext.getDbType();
        return DbType.DB2.getDb().equals(dbType);
    }
}
