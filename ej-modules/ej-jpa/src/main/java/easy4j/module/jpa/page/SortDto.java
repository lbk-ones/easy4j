package easy4j.module.jpa.page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * SortDto
 *
 * @author bokun.li
 * @date 2025-05
 */
@Setter
@Getter
@NoArgsConstructor
@Schema(name = "SortDto",description = "排序对象")
public class SortDto implements Serializable {
    public static final String DESC = "desc";
    public static final String ASC = "asc";
	//排序方式
    @Schema(description = "排序方式 desc | asc")
    private String orderType;

    //排序字段
    @Schema(description = "排序字段")
    private String orderField;

    public SortDto(String orderType, String orderField) {
        this.orderType = orderType;
        this.orderField = orderField;
    }

    //默认为DESC排序
    public SortDto(String orderField) {
        this.orderField = orderField;
        this.orderType = "desc";
    }
}