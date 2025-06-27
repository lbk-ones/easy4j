package template.service.storage.domains;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ADVICE_STORAGE")
public class AdviceStorage {
    @TableId
    private String ordCode;  // 项目代码
    private Integer count;   // 库存余额
    private Integer price;   // 单价
    private Integer frozeAmount;   // 冻结库存
}
