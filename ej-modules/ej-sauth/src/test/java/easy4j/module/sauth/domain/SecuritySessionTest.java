package easy4j.module.sauth.domain;

import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.dbaccess.TempDataSource;
import easy4j.infra.dbaccess.dynamic.dll.op.DynamicDDL;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

class SecuritySessionTest {



    DataSource getDataSource(){
        return new TempDataSource("org.postgresql.Driver","jdbc:postgresql://10.0.32.19:30163/ds","drhi_user","drhi_password");
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