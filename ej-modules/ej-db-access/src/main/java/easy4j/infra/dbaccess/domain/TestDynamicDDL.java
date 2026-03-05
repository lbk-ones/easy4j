package easy4j.infra.dbaccess.domain;

import easy4j.infra.dbaccess.annotations.JdbcColumn;
import easy4j.infra.dbaccess.annotations.JdbcTable;
import easy4j.infra.dbaccess.dynamic.dll.DDLField;
import easy4j.infra.dbaccess.dynamic.dll.DDLTable;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndex;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * ddl版本控制
 * select * from sys_ddl_version where ddl_name = ''
 */
@JdbcTable(name = "test_dynamic_ddl")
@Data
@Schema(description = "测试动态DDL生成的表")
@DDLTable(indexes = {
        @DDLIndex(keys = {"backField1", "backField2"}),
        @DDLIndex(keys = {"backField3"}),
})
public class TestDynamicDDL {

    @JdbcColumn(name = "id", isPrimaryKey = true, autoIncrement = true)
    @DDLField(comment = "主键", isAutoIncrement = true)
    private Long id;

    /**
     * 唯一索引 UNIQUE INDEX IDX_SYS_DDL_VERSION_DDL_NAME
     */
    @DDLField(comment = "唯一索引 UNIQUE INDEX IDX_SYS_DDL_VERSION_DDL_NAME")
    private String ddlName;

    /**
     * 版本
     */
    @DDLField(comment = "版本")
    private String ddlVersion;

    /**
     * 备注
     */
    @DDLField(comment = "备注")
    private String ddlRemark;

    /**
     * 是否成功
     */
    @DDLField(comment = "是否成功")
    private Integer success;

    /**
     * 耗时
     */
    @DDLField(comment = "耗时")
    private Long processTime;
    @DDLField(comment = "backField0")
    private byte backField0;
    @DDLField(comment = "backField1")
    private int backField1;
    @DDLField(comment = "backField2")
    private short backField2;
    @DDLField(comment = "backField3")
    private long backField3;
    @DDLField(comment = "backField4")
    private Byte backField4;
    @DDLField(comment = "backField6")
    private Short backField6;

    @DDLField(comment = "backField61")
    private java.sql.Date backField61;

    @DDLField(comment = "backField7")
    private LocalDateTime backField7;

    @DDLField(comment = "backField8")
    private LocalDate backField8;

    @DDLField(comment = "backField9")
    private LocalTime backField9;

    @DDLField(comment = "backField10")
    private BigDecimal backField10;
    @DDLField(comment = "backField11")
    private double backField11;

    @DDLField(comment = "backField12")
    private float backField12;

    @DDLField(comment = "backField13")
    private Double backField13;

    @DDLField(comment = "backField14")
    private Float backField14;

    @DDLField(comment = "测试unique的字段", dataLength = 25, isUnique = true)
    private String testUnique;

    @DDLField(comment = "测试unique的字段", dataLength = 25, check = "test_check in (0,1)")
    private byte testCheck;

    @DDLField(comment = "测试check的字段2", dataLength = 25, check = "test_check2 in (2,3)")
    private int testCheck2;

    private transient String testTransientSymbol;

    @Transient
    private String testTransientAnnotation;

    private final String testFinal = "";

    @DDLField(dataType = "jsonb", comment = "测试json")
    private String json;

    @DDLField(dataType = "jsonb", comment = "测试json2")
    private String json2;

    @DDLField(dataType = "enum", dataTypeAttr = {"0", "1", "2", "3"}, comment = "测试enum")
    private String testEnum;

    /**
     * 执行时间
     */
    @DDLField(defTime = true, isNotNull = true, comment = "执行时间")
    private Date exeDate;


    private Date testDate2;

    /**
     * 上一次执行时间
     */
    private Date lastExeDate;


}
