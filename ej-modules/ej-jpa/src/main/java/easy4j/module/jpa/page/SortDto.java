package easy4j.module.jpa.page;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
public class SortDto implements Serializable {
    public static final String DESC = "desc";
    public static final String ASC = "asc";
	//排序方式
    private String orderType;

    //排序字段
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
