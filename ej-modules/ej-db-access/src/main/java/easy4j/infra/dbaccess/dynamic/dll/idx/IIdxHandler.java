package easy4j.infra.dbaccess.dynamic.dll.idx;

/**
 * @see easy4j.infra.dbaccess.dynamic.dll.op.DynamicDDL
 */
@Deprecated
public interface IIdxHandler {

    boolean match(DDLIndexInfo ddlIndexInfo);

    String getName();

    String getIdx(DDLIndexInfo ddlIndexInfo);

}
