package easy4j.infra.dbaccess.dynamic.dll.ct;

import easy4j.infra.common.exception.EasyException;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.MySQLFieldType;
import easy4j.infra.dbaccess.dynamic.dll.ct.field.MysqlDDLFieldStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MysqlDDLFieldCoreTest {

    @Test
    void getDataTypeByMySQLFieldType() {

        MysqlDDLFieldStrategy mysqlDDLFieldCore = new MysqlDDLFieldStrategy();
        assertEquals("ENUM('xx1','xx2')", mysqlDDLFieldCore.getDataTypeByMySQLFieldType(
                MySQLFieldType.ENUM,
                new DDLFieldInfo().setDataType("enum").setDataTypeAttr(new String[]{"xx1", "xx2"})
        ));
        assertEquals("DATETIME", mysqlDDLFieldCore.getDataTypeByMySQLFieldType(
                MySQLFieldType.DATETIME,
                new DDLFieldInfo().setDataType("enum").setDataTypeAttr(new String[]{"xx1", "xx2"})
        ));
        assertEquals("DATETIME", mysqlDDLFieldCore.getDataTypeByMySQLFieldType(
                MySQLFieldType.DATETIME,
                new DDLFieldInfo().setDataType("xxx").setDataTypeAttr(new String[]{"xx1", "xx2"})
        ));
        assertEquals("DECIMAL(6,2)", mysqlDDLFieldCore.getDataTypeByMySQLFieldType(
                MySQLFieldType.DECIMAL,
                new DDLFieldInfo().setDataType("xxx").setDataLength(6).setDataDecimal(2))
        );
        assertEquals("DECIMAL(6,2)", mysqlDDLFieldCore.getDataTypeByMySQLFieldType(
                MySQLFieldType.DECIMAL,
                new DDLFieldInfo().setDataType("xxx").setDataTypeAttr(new String[]{"6", "2"}))
        );
        assertEquals("VARCHAR(34)", mysqlDDLFieldCore.getDataTypeByMySQLFieldType(
                MySQLFieldType.VARCHAR,
                new DDLFieldInfo().setDataType("xxx").setDataLength(34).setDataDecimal(2))
        );

        assertEquals("BIT(1)", mysqlDDLFieldCore.getDataTypeByMySQLFieldType(
                MySQLFieldType.BIT,
                new DDLFieldInfo().setDataType("xxx").setDataDecimal(2))
        );
        assertEquals("BIT(44)", mysqlDDLFieldCore.getDataTypeByMySQLFieldType(
                MySQLFieldType.BIT,
                new DDLFieldInfo().setDataType("xxx").setDataLength(44).setDataDecimal(2))
        );

        assertThrows(EasyException.class, () -> mysqlDDLFieldCore.getDataTypeByMySQLFieldType(
                MySQLFieldType.BINARY,
                new DDLFieldInfo().setDataType("xxx"))
        );
        assertThrows(EasyException.class, () -> mysqlDDLFieldCore.getDataTypeByMySQLFieldType(
                MySQLFieldType.VARBINARY,
                new DDLFieldInfo().setDataType("xxx"))
        );
        assertThrows(EasyException.class, () -> mysqlDDLFieldCore.getDataTypeByMySQLFieldType(
                MySQLFieldType.SET,
                new DDLFieldInfo().setDataType("xxx"))
        );
        assertThrows(EasyException.class, () -> mysqlDDLFieldCore.getDataTypeByMySQLFieldType(
                MySQLFieldType.ENUM,
                new DDLFieldInfo().setDataType("xxx"))
        );
    }
}