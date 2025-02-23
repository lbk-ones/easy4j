package easy4j.module.idempotent.rules;

import easy4j.module.base.plugin.idempotent.Easy4jIdempotentKeyGenerator;

import javax.servlet.http.HttpServletRequest;

public class HeaderEasy4jIdempotentKeyGenerator implements Easy4jIdempotentKeyGenerator {
    public static final String IDEMPOTENT_HEADER_KEY = "XIdempotentKey";
    @Override
    public String generate(HttpServletRequest request) {

        return request.getHeader(IDEMPOTENT_HEADER_KEY);
    }
}
