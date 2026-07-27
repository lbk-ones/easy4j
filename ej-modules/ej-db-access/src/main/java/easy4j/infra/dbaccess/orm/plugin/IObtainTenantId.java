package easy4j.infra.dbaccess.orm.plugin;

import easy4j.infra.dbaccess.orm.RuntimeContext;

public interface IObtainTenantId {

    Object getTenantId(RuntimeContext<?> context);

}
