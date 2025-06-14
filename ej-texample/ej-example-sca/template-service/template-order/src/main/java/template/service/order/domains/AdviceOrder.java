package template.service.order.domains;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("ADVICE_ORDER")
public class AdviceOrder {
    @TableId
    private String orderNo;   // 申请单号
    @Schema(description = "患者ID")
    private String patId;     // 患者ID
    @Schema(description = "项目代码")
    private String ordCode;   // 项目代码
    @Schema(description = "申请数量")
    private Integer num;   // 申请数量
}
