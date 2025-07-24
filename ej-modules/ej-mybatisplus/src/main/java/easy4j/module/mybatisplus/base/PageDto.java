package easy4j.module.mybatisplus.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public class PageDto implements Serializable {


    // 从1开始
    @Schema(description = "页码 从1开始")
    private int pageNo = 1;
    // 默认20条
    @Schema(description = "每页多少条，默认20")
    private int pageSize = 20;

    private String searchKey;

    @Schema(description = "过滤的条件，格式为二维数组的集合 [[\"status\",\"eq\",\"xxx\"]] 支持 eq in like likeLeft likeRight,in的话第三个参数为json类型的字符串数组")
    private List<List<Object>> keys;

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public void setKeys(List<List<Object>> keys) {
        this.keys = keys;
    }
}
