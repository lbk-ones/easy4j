package easy4j.module.mybatisplus.codegen.servlet;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.*;

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

    // 分页API
    public String pageApiUrl;

    // 添加APi
    public String formAddApiUrl;

    // 更新APi
    public String formUpdateApiUrl;

    // 删除
    public String formDeleteApiUrl;

    // 启用禁用
    public String enableOrDisabledUrl;

    // controller.req 下面的传参名称
    public String controllerReqDtoName;

    // 所有的api地址
    public List<API> allApiUrl = new ArrayList<>();

    // 操作按钮
    public List<ACTION> actions = new ArrayList<>();

    // 字段信息
    public List<ColumnInfo> columns = new ArrayList<>();

    @Data
    @Accessors(chain = true)
    public static class ACTION {
        public String key;
        public String label;
        public String message;
        public String status;
        public String type = "outline";
        public String confirmMessage;
        public boolean isFetchData = true;
        public boolean needSelect = true;


        public ACTION(String key, String label) {
            this.key = key;
            this.label = label;
        }
    }

    @Data
    public static class ColumnInfo implements Serializable {

        private String title;
        private String dataIndex;
        private Integer width;
        private Boolean ellipsis = true;
        private Boolean visible = true;
        private Form form = new Form();

        @Data
        public static class Form {
            // boolean 值则是 switch
            // 数字 则是 number
            private String type;
            private boolean creatable = true;      // 新增时是否显示
            private boolean editable = true;       // 编辑时是否显示
            private boolean required = false;       // 是否必填
            private boolean oneRow = false;       // 大文本
            private String placeholder;
            private Object defaultValue;    // 默认值
            private String enterNext;     // 回车后跳转到的下一个字段名（提升录入体验）

            private Map<String,Object> attrs = new HashMap<>();
        }
    }

    @Data
    public static class API {

        public String url;

        // 简单介绍
        private String summary;

        // 描述
        private String description;

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            API api = (API) object;
            return StrUtil.equals(url, api.url) && StrUtil.equals(summary, api.summary) && StrUtil.equals(description, api.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, summary, description);
        }
    }


}
