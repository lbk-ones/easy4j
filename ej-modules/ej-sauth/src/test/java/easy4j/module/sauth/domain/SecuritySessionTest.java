package easy4j.module.sauth.domain;

import easy4j.infra.dbaccess.TempDataSource;
import easy4j.infra.dbaccess.dll.op.DynamicDDL;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

class SecuritySessionTest {


    DataSource getDataSource() {
        return new TempDataSource("org.postgresql.Driver", "jdbc:postgresql://10.0.32.19:30163/ds", "drhi_user", "drhi_password");
    }

    @Test
    void getUserId() {
        String s;
        try (DynamicDDL dynamicDDL = new DynamicDDL(getDataSource(), null, SecuritySession.class)) {
            s = dynamicDDL.autoDDLByJavaClass(true);
        }
        System.out.println(s);
    }
}