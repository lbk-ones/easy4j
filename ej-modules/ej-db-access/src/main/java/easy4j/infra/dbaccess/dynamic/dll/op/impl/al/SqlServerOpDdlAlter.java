package easy4j.infra.dbaccess.dynamic.dll.op.impl.al;

import easy4j.infra.common.enums.DbType;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;

public class SqlServerOpDdlAlter extends AbstractOpDdlAlter {

    @Override
    public boolean match(OpContext opContext) {
        String dbType = opContext.getDbType();
        return DbType.SQL_SERVER.getDb().equals(dbType);
    }

    @Override
    public String getRenameColumnTemplate() {
        return "EXEC sp_rename '[" + TABLE_NAME + "].[" + COLUMN_NAME + "]', '[" + NEW_COLUMN_NAME + "]', 'COLUMN';";
    }
}
