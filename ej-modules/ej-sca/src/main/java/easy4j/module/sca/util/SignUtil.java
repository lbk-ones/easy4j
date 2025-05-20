package easy4j.module.sca.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSONObject;
import easy4j.module.base.exception.EasyException;
import easy4j.module.sca.config.Easy4jCloudBaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import java.util.SortedMap;

/**
 * 签名工具类
 */
@Slf4j
public class SignUtil {
    public static final String X_PATH_VARIABLE = "x-path-variable";
    /**
     * 符号：美元 $
     */
    private static final String DOLLAR = "$";
    /**
     * 符号：左花括号 }
     */
    private static final String LEFT_CURLY_BRACKET = "{";

    /**
     * @param params 所有的请求参数都会在这里进行排序加密
     * @return 验证签名结果
     */
    public static boolean verifySign(SortedMap<String, String> params, String headerSign) {
        if (params == null || StrUtil.isEmpty(headerSign)) {
            return false;
        }
        // 把参数加密
        String paramsSign = getParamsSign(params);
        log.info("Param Sign : {}", paramsSign);
        return !StrUtil.isEmpty(paramsSign) && headerSign.equals(paramsSign);
    }

    /**
     * @param params 所有的请求参数都会在这里进行排序加密
     * @return 得到签名
     */
    public static String getParamsSign(SortedMap<String, String> params) {
        //去掉 Url 里的时间戳
        params.remove("_t");
        String paramsJsonStr = JSONObject.toJSONString(params);
        log.info("Param paramsJsonStr : {}", paramsJsonStr);
        //设置签名秘钥
        Easy4jCloudBaseConfig easy4jCloudBaseConfig = SpringUtil.getBean(Easy4jCloudBaseConfig.class);
        String signatureSecret = easy4jCloudBaseConfig.getSignatureSecret();
        String curlyBracket = SignUtil.DOLLAR + SignUtil.LEFT_CURLY_BRACKET;
        if (StrUtil.isEmpty(signatureSecret) || signatureSecret.contains(curlyBracket)) {
            log.error("签名密钥 ${easy4j.signatureSecret} 未配置 ！");
            throw new EasyException("签名密钥 ${easy4j.signatureSecret} 未配置 ！");
        }
        return DigestUtils.md5DigestAsHex((paramsJsonStr + signatureSecret).getBytes()).toUpperCase();
    }
}
