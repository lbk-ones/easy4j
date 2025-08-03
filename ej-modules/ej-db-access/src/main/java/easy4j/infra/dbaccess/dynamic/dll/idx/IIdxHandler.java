package easy4j.infra.dbaccess.dynamic.dll.idx;

public interface IIdxHandler {

    boolean match(DDLIndexInfo ddlIndexInfo);

    String getName();

    String getIdx(DDLIndexInfo ddlIndexInfo);

}
