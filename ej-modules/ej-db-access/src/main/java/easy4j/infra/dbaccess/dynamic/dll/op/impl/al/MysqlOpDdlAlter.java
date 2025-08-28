package easy4j.infra.dbaccess.dynamic.dll.op.impl.al;

import easy4j.infra.common.enums.DbType;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;

public class MysqlOpDdlAlter extends AbstractOpDdlAlter {

    @Override
    public boolean match(OpContext opContext) {
        String dbType = opContext.getDbType();
        return DbType.MYSQL.getDb().equals(dbType);
    }

    @Override
    public String getRenameColumnTemplate() {
        return "alter table [" + TABLE_NAME + "] change column [" + COLUMN_NAME + "] [" + NEW_COLUMN_NAME + "] [" + COLUMN_CONSTRAINT + "]";
    }
}
