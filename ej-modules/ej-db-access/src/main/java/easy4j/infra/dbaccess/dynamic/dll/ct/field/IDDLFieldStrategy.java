package easy4j.infra.dbaccess.dynamic.dll.ct.field;

import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;

/**
 * @see easy4j.infra.dbaccess.dynamic.dll.op.DynamicDDL
 */
@Deprecated
public interface IDDLFieldStrategy {

    boolean match(DDLFieldInfo ddlFieldInfo);


    String getResColumn(DDLFieldInfo ddlFieldInfo);

}
