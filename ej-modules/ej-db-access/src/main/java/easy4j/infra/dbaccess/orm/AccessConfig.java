package easy4j.infra.dbaccess.orm;

import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import jakarta.persistence.Access;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.sql.DataSource;

@Data
@Accessors(chain = true)
public class AccessConfig {

    // 数据源
    private DataSource dataSource;

    // 是否加入当前事务
    private boolean inTransaction;

    // 是否打印sql
    private boolean printSqlIs;

    // 字段名称是否转下划线
    private boolean fieldNameToUnderline;

    private String dbType;

}
