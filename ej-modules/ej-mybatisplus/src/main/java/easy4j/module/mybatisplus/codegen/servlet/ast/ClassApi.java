package easy4j.module.mybatisplus.codegen.servlet.ast;

import lombok.Data;

@Data
public class ClassApi {

    // 前缀
    private String prefix;

    // 路径
    private String path;

    // 全路径
    private String url;

    // 简单介绍
    private String summary;

    // 描述
    private String description;

    @Override
    public String toString() {
        return "ClassApi{" +
                "prefix='" + prefix + '\'' +
                ", path='" + path + '\'' +
                ", url='" + url + '\'' +
                ", summary='" + summary + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
