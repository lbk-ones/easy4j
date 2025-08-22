package easy4j.infra.dbaccess.dynamic.dll.op.api;

import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;

public interface OpSqlCommands extends IOpContext {

    void addColumn(DDLFieldInfo fieldInfo);

    void removeColumn(DDLFieldInfo fieldInfo);

    void renameColumnName(DDLFieldInfo fieldInfo, String newColumnName);

}
