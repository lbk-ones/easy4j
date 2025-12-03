package easy4j.infra.rpc.registry.jdbc;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 管理临时数据
 */
public class EphemeralDataManager implements AutoCloseable {

    final JdbcOperate jdbcOperate;
    List<Long> ephemeralIds = new CopyOnWriteArrayList<>();

    public EphemeralDataManager(JdbcOperate jdbcOperate) {
        this.jdbcOperate = jdbcOperate;
    }

    public void addEphemeralId(Long id) {
        if (null != id) ephemeralIds.add(id);
    }

    @Override
    public void close() throws Exception {
        for (Long ephemeralId : ephemeralIds) {
            SysE4jJdbcRegData sysE4jJdbcRegData = new SysE4jJdbcRegData();
            sysE4jJdbcRegData.setId(ephemeralId);
            jdbcOperate.delete(sysE4jJdbcRegData);
        }
        ephemeralIds.clear();
    }
}
