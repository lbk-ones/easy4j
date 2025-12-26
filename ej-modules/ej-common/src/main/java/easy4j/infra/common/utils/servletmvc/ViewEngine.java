package easy4j.infra.common.utils.servletmvc;

import cn.hutool.core.util.StrUtil;

public enum ViewEngine {
    THYMELEAF,
    FREEMARKER;
    public static ViewEngine of(String type){

        ViewEngine[] values = ViewEngine.values();
        for (ViewEngine value : values) {
            if (StrUtil.equalsIgnoreCase(value.name(),type)) {
                return value;
            }
        }
        throw new RuntimeException("not support view engine");
    }
}
