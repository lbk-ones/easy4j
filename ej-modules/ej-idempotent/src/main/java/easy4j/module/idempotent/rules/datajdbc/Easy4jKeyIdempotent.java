package easy4j.module.idempotent.rules.datajdbc;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.util.Date;

@Data
@Table("KEY_IDEMPOTENT")
public class Easy4jKeyIdempotent implements Serializable {

    // 正在发送
    public static final Integer PENDING = 0;
    // 已发送
    public static final Integer SENT = 1;

    // 失败
    public static final Integer FAILED = 2;

    @Id
    @Column("IDE_KEY")
    private String ideKey;

    @Column("EXPIRE_DATE")
    private Date expireDate;

}
