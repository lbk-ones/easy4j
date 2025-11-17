package easy4j.module.sauth.authentication;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.module.seed.CommonKey;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * jwt Token 生成统一封装
 *
 * @author bokun.li
 * @date 2025/11/14
 */
public class JWTUtils {

    /**
     * 登录账号
     */
    public static final String JWT_USER_NAME_KEY = "un";

    /**
     * 中文名称
     */
    public static final String JWT_USER_NAME_CN_KEY = "cn";
    public static final String JWT_USER_NAME_EXPIRE_TIME = "exp";
    public static final String JWT_JTI_KEY = "jti";


    /**
     * 生成 jwt token 字符串
     *
     * @param username
     * @param userNameCn
     * @param expireTime
     * @param signSecret
     * @return
     */
    public static String genJwtToken(String username, String userNameCn, long expireTime, String signSecret) {
        if (StrUtil.isBlank(signSecret))
            signSecret = Easy4j.getProperty(Easy4j.getEjSysPropertyName(EjSysProperties::getJwtSecret), "Unknown");
        // unique
        String s = CommonKey.gennerString();
        Map<String, Object> claims = new HashMap<>();
        claims.put(JWT_USER_NAME_KEY, username);
        claims.put(JWT_USER_NAME_CN_KEY, userNameCn);
        claims.put(JWT_USER_NAME_EXPIRE_TIME, expireTime);
        claims.put(JWT_JTI_KEY, s);
        return JWT.create()
                .addPayloads(claims)
                .setKey(signSecret.getBytes(StandardCharsets.UTF_8))
                .sign();
    }

    /**
     * 验签jwtToken字符串
     *
     * @param token
     * @param signSecret
     * @return
     */
    public static boolean verify(String token, String signSecret) {
        if (StrUtil.isBlank(signSecret))
            signSecret = Easy4j.getProperty(Easy4j.getEjSysPropertyName(EjSysProperties::getJwtSecret), "Unknown");
        return JWT.of(token).setKey(signSecret.getBytes(StandardCharsets.UTF_8)).verify();
    }

}
