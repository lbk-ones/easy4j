package easy4j.module.base.plugin.idempotent;

import javax.servlet.http.HttpServletRequest;

public interface Easy4jIdempotentKeyGenerator {
    String generate(HttpServletRequest request);
}