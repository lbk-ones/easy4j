package easy4j.infra.dbaccess.dynamic.dll.op.api;

import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;

public interface MetaInfoParse extends IOpContext {


    DDLTableInfo parse();

}
