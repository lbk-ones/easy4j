package template.service.api.dto;

import lombok.Data;

@Data
public class AdviceStorageDto {
    private String ordCode;  // 项目代码
    private Integer count;   // 库存余额
    private Integer price;   // 单价
}
