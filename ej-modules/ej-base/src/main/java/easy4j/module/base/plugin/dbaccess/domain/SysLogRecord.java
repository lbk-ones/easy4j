package easy4j.module.base.plugin.dbaccess.domain;

import easy4j.module.base.plugin.dbaccess.annotations.JdbcColumn;
import lombok.Data;
import org.apache.commons.dbutils.Column;

import java.io.Serializable;
import java.util.Date;


@Data
public class SysLogRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键
    @JdbcColumn(isPrimaryKey = true)
    private String id;

    // 日志标签
    private String tag;

    // 标签描述
    private String tagDesc;

    // 链路ID
    private String traceId;

    // 处理状态
    private String status;

    // 处理时间
    private String processTime;

    // 操作时间(长文本)
    // 索引 IDX_SYS_LOG_RECORD_CREATE_DATE
    private Date createDate;

    // 参数(长文本)
    private String params;

    // 备注(长文本)
    private String remark;

    // 错误信息
    private String errorInfo;
}
