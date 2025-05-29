package easy4j.module.base.plugin.idempotent;

import javax.servlet.http.HttpServletRequest;

/**
 * Easy4jIdempotentKeyGenerator
 *
 * @author bokun.li
 * @date 2025-05
 */
public interface Easy4jIdempotentKeyGenerator {
    String generate(HttpServletRequest request);
}