package easy4j.infra.dbaccess.dynamic.dll.op.impl.sc;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

@Accessors(chain = true)
@Data
public class CopyDbConfig {

    @NotNull
    private DataSource dataSource;

    private String schema;

    private String connection;

    // 表前缀
    private String tablePrefix;

    // 表后缀
    private String tableSuffix;

    // 是否执行
    private boolean isExe;

}
