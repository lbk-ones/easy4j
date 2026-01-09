package easy4j.module.mybatisplus.codegen.servlet;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 传入dto 和 domain类型
 * 分析出主键和表名以及中文描述
 * 扫描所有api地址
 */
@Data
public class PageViewRes implements Serializable {

    // 唯一ID
    public String uniqueId;

    // 中文描述
    public String cnDesc;

    // 主键字段
    public String rowKey;

    // 所有的api地址
    public List<String> allApiUrl = new ArrayList<>();

    // 字段信息
    public List<ColumnInfo> columns = new ArrayList<>();

    @Data
    public static class ColumnInfo implements Serializable{

        private String title;

        private String dataIndex;

        // boolean 值则是 switch
        // 数字 则是 number
        private String type;

    }



}
