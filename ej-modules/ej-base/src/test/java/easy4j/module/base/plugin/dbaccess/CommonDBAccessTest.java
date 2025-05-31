package easy4j.module.base.plugin.dbaccess;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import easy4j.module.base.exception.EasyException;

class CommonDBAccessTest {


    private final CommonDBAccess dbAccess = new CommonDBAccess() {
        // 匿名子类实现抽象类
    };

    // 测试 DDL
    @Test
    void DDlLine() {

        String sql = dbAccess.DDlLine("SELECT", "users", "id = 1");
        assertEquals("SELECT * FROM users id = 1", sql);
        String sql2 = dbAccess.DDlLine("SELECT", "users", "id = 1", "id", "name");
        assertEquals("SELECT id, name FROM users id = 1", sql2);

        String sql3 = dbAccess.DDlLine("UPDATE", "users", "name = 'Alice' WHERE id = 1");
        assertEquals("UPDATE users SET name = 'Alice' WHERE id = 1", sql3);


        String sql31 = dbAccess.DDlLine("UPDATE", "users", "WHERE id = 1", "name = 'Alice'", "age = 12");
        assertEquals("UPDATE users SET name = 'Alice', age = 12 WHERE id = 1", sql31);

        String sql32 = dbAccess.DDlLine("UPDATE", "users", "XXXX WHERE id = 1");
        assertEquals("UPDATE users SET XXXX WHERE id = 1", sql32);

        String sql33 = dbAccess.DDlLine("UPDATE", "users", null);
        assertEquals("UPDATE users SET", sql33);

        String sql4 = dbAccess.DDlLine("DELETE", "users", "id = 1");
        assertEquals("DELETE FROM users id = 1", sql4);

        String sql5 = dbAccess.DDlLine("INSERT", "users", "VALUES ('Alice')", "name");
        assertEquals("INSERT INTO users name VALUES ('Alice')", sql5);

        String sql6 = dbAccess.DDlLine("INSERT", "users", "VALUES (1, 'Alice')", "id", "name");
        assertEquals("INSERT INTO users (id, name) VALUES (1, 'Alice')", sql6);

        String sql61 = dbAccess.DDlLine("INSERT", "users", "VALUES (1, 'Alice')");
        assertEquals("INSERT INTO users VALUES (1, 'Alice')", sql61);


        String sql7 = dbAccess.DDlLine("select", "users", "id = 1");
        assertEquals("SELECT * FROM users id = 1", sql7);


        assertThrows(EasyException.class, () -> {
            dbAccess.DDlLine(null, "users", "id = 1");
        });

        assertThrows(EasyException.class, () -> {
            dbAccess.DDlLine("SELECT", null, "id = 1");
        });


        String sql9 = dbAccess.DDlLine("SELECT", "users", null);
        assertEquals("SELECT * FROM users", sql9);


        assertThrows(EasyException.class, () -> {
            dbAccess.DDlLine("UPSERT", "users", "id = 1");
        });
    }
}