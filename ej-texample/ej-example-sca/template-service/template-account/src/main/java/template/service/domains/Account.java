package template.service.domains;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ACCOUNT")
public class Account {
    @TableId
    private String patId;    // 患者ID
    private Integer balance; // 余额
}