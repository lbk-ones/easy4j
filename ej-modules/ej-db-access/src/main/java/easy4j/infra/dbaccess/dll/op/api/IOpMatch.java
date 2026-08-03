package easy4j.infra.dbaccess.dll.op.api;

import easy4j.infra.dbaccess.dll.op.OpContext;

public interface IOpMatch {
    boolean match(OpContext opContext);
}
