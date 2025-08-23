package easy4j.infra.dbaccess.dynamic.dll;

@Deprecated
public interface DDLParse {

    void execDDL();

    String getDDLFragment();
}
