package easy4j.infra.dbaccess.dynamic.dll.op.api;

import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;

public interface IOpMatch {
    boolean match(OpContext opContext);
}
