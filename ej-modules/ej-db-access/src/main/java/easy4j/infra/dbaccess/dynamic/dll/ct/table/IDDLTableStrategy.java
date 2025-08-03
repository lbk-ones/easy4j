package easy4j.infra.dbaccess.dynamic.dll.ct.table;

import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;

import java.util.List;

public interface IDDLTableStrategy {

    void setToUnderLine(boolean toUnderLine);

    boolean match(DDLTableInfo ddlTableInfo);


    String getTableTemplate(DDLTableInfo ddlFieldInfo);

    List<String> getComments(DDLTableInfo ddlFieldInfo);

    List<String> getIndexes(DDLTableInfo ddlFieldInfo);

}
