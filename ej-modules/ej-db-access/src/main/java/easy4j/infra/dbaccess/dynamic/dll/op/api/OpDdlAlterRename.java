package easy4j.infra.dbaccess.dynamic.dll.op.api;

public interface OpDdlAlterRename extends OpDdlAlter {

    void renameColumnName(String newColumnName);

    void renameConstraintName(String newConstraintName);

    void renameTableName(String newTableName);

    void setSchemaNewName(String schemaNewName);

    void setNewTableSpace(String newTableSpaceName);


}
