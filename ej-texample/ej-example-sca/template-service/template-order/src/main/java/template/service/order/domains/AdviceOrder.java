package template.service.order.domains;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@TableName("ADVICE_ORDER")
public class AdviceOrder {
    @TableId
    private String orderNo;   // 申请单号
    @Schema(description = "患者ID")
    private String patId;     // 患者ID
    @Schema(description = "项目代码")
    @NotEmpty(message = "项目代码不能为空")
    private String ordCode;   // 项目代码


    @NotNull(message = "申请数量不能为空")
    @Schema(description = "申请数量")
    private Integer num;   // 申请数量
}
