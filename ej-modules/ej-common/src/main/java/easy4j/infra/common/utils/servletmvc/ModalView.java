package easy4j.infra.common.utils.servletmvc;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

@Data
@Accessors(chain = true)
public class ModalView implements Serializable {

    private String view;

    private Map<String, Object> modal;

    /**
     * 默认使用 THYMELEAF
     */
    private ViewEngine viewEngine;

    private MimeType mimeType = MimeType.TEXT_HTML;
}
