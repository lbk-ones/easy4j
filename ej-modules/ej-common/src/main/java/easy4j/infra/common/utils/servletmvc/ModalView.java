package easy4j.infra.common.utils.servletmvc;

import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
public class ModalView implements Serializable {

    private String view;

    private Map<String, Object> modal = new HashMap<>();

    /**
     * 默认使用 THYMELEAF
     */
    private ViewEngine viewEngine;

    private MimeType mimeType = MimeType.TEXT_HTML;

    public static ModalView of(String viewPath) {
        return new ModalView().setView(viewPath);
    }

    public static ModalView of(String viewPath, String key, Object value) {
        return new ModalView().setView(viewPath).put(key, value);
    }

    public static ModalView of(String viewPath, Object object) {
        return new ModalView().setView(viewPath).fromObj(object);
    }

    public static ModalView of(String viewPath, Map<String, Object> object) {
        return new ModalView().setView(viewPath).setModal(object);
    }

    public ModalView put(String name, Object obj) {
        modal.put(name, obj);
        return this;
    }

    public ModalView fromObj(Object object) {
        Map<String, Object> parseMap = BeanUtil.beanToMap(object);
        if (parseMap != null && !parseMap.isEmpty()) modal.putAll(parseMap);
        return this;
    }
}
