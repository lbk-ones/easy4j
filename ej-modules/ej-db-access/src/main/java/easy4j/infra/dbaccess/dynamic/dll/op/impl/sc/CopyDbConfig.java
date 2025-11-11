package easy4j.infra.dbaccess.dynamic.dll.op.impl.sc;

import easy4j.infra.common.annotations.Desc;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.sql.DataSource;

import jakarta.validation.constraints.NotNull;

import java.sql.Connection;

@Accessors(chain = true)
@Data
public class CopyDbConfig {

    @NotNull
    private DataSource dataSource;

    private String schema;

    private Connection connection;

    // 表前缀
    private String tablePrefix;

    // 表后缀
    private String tableSuffix;

    // 是否执行
    private boolean isExe;

    // 是否表名强制转义
    private boolean escapeTableName;


}
