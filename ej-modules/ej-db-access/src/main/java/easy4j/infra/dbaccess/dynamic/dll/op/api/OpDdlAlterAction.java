package easy4j.infra.dbaccess.dynamic.dll.op.api;

public interface OpDdlAlterAction extends OpDdlAlter {

    void addColumn();

    void dropColumn();

}
