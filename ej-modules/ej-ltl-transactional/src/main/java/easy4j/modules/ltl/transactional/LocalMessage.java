package easy4j.modules.ltl.transactional;

import easy4j.module.base.plugin.dbaccess.annotations.JdbcColumn;
import easy4j.module.base.plugin.dbaccess.annotations.JdbcTable;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table("LOCAL_MESSAGE")
@JdbcTable(name = "LOCAL_MESSAGE")
public class LocalMessage implements Serializable {

    // 正在发送
    public static final Integer PENDING = 0;
    // 已发送
    public static final Integer SENT = 1;

    // 失败
    public static final Integer FAILED = 2;

    @Id
    @Column("MSG_ID")
    @JdbcColumn(name = "MSG_ID", isPrimaryKey = true)
    private String msgId;
    @Column("BUSINESS_KEY")
    private String businessKey;
    @Column("BUSINESS_NAME")
    private String businessName;
    @Column("CONTENT")
    private String content;
    @Column("BEAN_NAME")
    private String beanName;
    @Column("BEAN_METHOD")
    private String beanMethod;
    @Column("RETRY_COUNT")
    private Integer retryCount;
    @Column("STATUS")
    private Integer status;
    @Column("CREATE_DATE")
    private LocalDateTime createDate;
    @Column("UPDATE_DATE")
    private LocalDateTime updateDate;

    @Column("ERROR_MESSAGE")
    private String errorMessage;

    @Column("IS_FREEZE")
    private String isFreeze;
}
