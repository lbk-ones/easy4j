package easy4j.infra.dbaccess.domain;

import easy4j.infra.dbaccess.annotations.JdbcColumn;
import easy4j.infra.dbaccess.annotations.JdbcTable;
import lombok.Data;

import java.util.Date;

/**
 * ddl版本控制
 * select * from sys_ddl_version where ddl_name = ''
 */
@JdbcTable(name = "sys_ddl_history")
@Data
public class SysDdlHistory {

    @JdbcColumn(name = "id", isPrimaryKey = true)
    private Long id;

    /**
     * 唯一索引 UNIQUE INDEX IDX_SYS_DDL_VERSION_DDL_NAME
     */
    private String ddlName;

    /**
     * 版本
     */
    private String ddlVersion;

    /**
     * 备注
     */
    private String ddlRemark;

    /**
     * 是否成功
     */
    private Integer success;

    /**
     * 耗时
     */
    private Integer processTime;

    /**
     * 执行时间
     */
    private Date exeDate;

    /**
     * 上一次执行时间
     */
    private Date lastExeDate;


}
