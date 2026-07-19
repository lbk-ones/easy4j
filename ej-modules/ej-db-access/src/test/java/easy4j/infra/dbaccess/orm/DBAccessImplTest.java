package easy4j.infra.dbaccess.orm;

import easy4j.infra.common.utils.SqlType;
import easy4j.infra.dbaccess.TempDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

class DBAccessImplTest {

    IDBAccess idbAccess;

    @BeforeEach
    void setUp() {
        AccessConfig accessConfig = new AccessConfig();
        accessConfig.setDataSource(getMysql8DataSource());
        idbAccess = new DBAccessImpl(accessConfig);
    }

    public DataSource getMysql8DataSource(){
        String jdbcUrl = "jdbc:mysql://219.100.186.4:3306/user_test";
        String driverClassNameByUrl = SqlType.getDriverClassNameByUrl(jdbcUrl);
        return new TempDataSource(driverClassNameByUrl, jdbcUrl, "root", "xx");
    }

    @Test
    void save() {
        SysUser sysUser = SysUserBuilderUtil.buildTestSysUser();
        SysUser save = idbAccess.save(sysUser, SysUser.class);

    }

    @Test
    void testSave() {
    }

    @Test
    void delete() {
    }

    @Test
    void deleteById() {
    }
}