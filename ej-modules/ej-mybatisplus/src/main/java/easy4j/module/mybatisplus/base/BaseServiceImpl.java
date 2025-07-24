package easy4j.module.mybatisplus.base;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.json.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * service层父类
 *
 * @author bokun.li
 * @date 2025/7/23
 */
public class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * 获取lambda表达式的字段名称
     *
     * @author bokun.li
     * @date 2025/7/23
     */
    protected String getFn(Func1<T, ?> func1) {
        return LambdaUtil.getFieldName(func1);
    }


    /**
     * 解析二位数组到QueryWrapper查询条件去
     * <br/>
     * 前端查询一般来说就是 eq in like gt gte lt lte tgt tgte tlt tlte between 几种条件
     * <br/>
     * tgt tgte tlt tlte 这个是时间相关的比较
     * <br/>
     * eq : ["xx","eq","xxx"] 相等
     * <br/>
     * in : ["xx","in","[\"33\",\"22\"]"] 多个相等
     * <br/>
     * like|likeLeft|likeRight: ["xx","like|likeLeft|likeRight","xxx"] 几种模糊查询
     * <br/>
     * gt : ["xx","gt","xxx"] 大于
     * <br/>
     * gte: ["xx","gte","xxx"] 大于等于
     * <br/>
     * tgt: ["xx","tgt","yyyy-MM-dd HH:mm:ss"] 时间格式（支持大多数时间格式）大于
     * <br/>
     * tgte: ["xx","tgte","yyyy-MM-dd HH:mm:ss"] 时间格式（支持大多数时间格式）大于等于
     * <br/>
     * tlt: ["xx","tlt","yyyy-MM-dd HH:mm:ss"] 时间格式（支持大多数时间格式）大于等于
     * <br/>
     * tlte: ["xx","tlte","yyyy-MM-dd HH:mm:ss"] 时间格式（支持大多数时间格式）大于等于
     * <br/>
     * between: ["xx","between","yyyy-MM-dd HH:mm:ss","yyyy-MM-dd HH:mm:ss"] 时间范围开区间
     * <br/>
     * betweene: ["xx","betweene","yyyy-MM-dd HH:mm:ss","yyyy-MM-dd HH:mm:ss"] 时间范围闭区间
     *
     * @param pageDto
     * @param queryWrapper
     */
    public void parsePageKeysToQuery(PageDto pageDto, QueryWrapper<T> queryWrapper) {
        List<List<Object>> keys = pageDto.getKeys();
        parseKeysWith(queryWrapper, keys, true);
    }
    public void parsePageKeysToQuery(PageDto pageDto, QueryWrapper<T> queryWrapper, boolean toUnderLine) {
        List<List<Object>> keys = pageDto.getKeys();
        parseKeysWith(queryWrapper, keys, toUnderLine);
    }
    public void parseKeysToQuery(List<List<Object>> keys, QueryWrapper<T> queryWrapper) {
        parseKeysWith(queryWrapper, keys, true);
    }
    public void parseKeysToQuery(List<List<Object>> keys, QueryWrapper<T> queryWrapper,boolean toUnderLine) {
        parseKeysWith(queryWrapper, keys, toUnderLine);
    }

    private void parseKeysWith(QueryWrapper<T> queryWrapper, List<List<Object>> keys, boolean toUnderLine) {
        if (CollUtil.isEmpty(keys)) {
            return;
        }
        for (List<Object> key : keys) {
            try{
                String s = StrUtil.trim(key.get(0).toString());
                String s2 = StrUtil.trim(key.get(1).toString());
                Object s3 = key.get(2);
                if (StrUtil.hasBlank(s, s2) || null == s3) {
                    continue;
                }
                if(toUnderLine){
                    s = StrUtil.toUnderlineCase(s);
                }
                switch (s2) {
                    case "eq":
                        queryWrapper.eq(s, s3);
                        break;
                    case "in":
                        try {
                            ArrayList<String> object = JacksonUtil.toObject(s3.toString(), new TypeReference<ArrayList<String>>() {
                            });
                            if (CollUtil.isNotEmpty(object)) {
                                queryWrapper.in(s, object);
                            }
                        } catch (Throwable e) {
                            throw EasyException.wrap(BusCode.A000031,"query in values is error!");
                        }
                        break;
                    case "like":
                        queryWrapper.like(s, s3);
                        break;
                    case "likeLeft":
                        queryWrapper.likeLeft(s, s3);
                        break;
                    case "likeRight":
                        queryWrapper.likeRight(s, s3);
                        break;
                    case "lt":
                        queryWrapper.lt(false, s, s3);
                        break;
                    case "lte":
                        queryWrapper.lt(true, s, s3);
                        break;
                    case "gt":
                        queryWrapper.gt(false, s, s3);
                        break;
                    case "gte":
                        queryWrapper.gt(true, s, s3);
                        break;
                    case "tgt":
                        queryWrapper.gt(false, s, DateUtil.parse(s3.toString()));
                        break;
                    case "tgte":
                        queryWrapper.gt(true, s, DateUtil.parse(s3.toString()));
                        break;
                    case "tlt":
                        queryWrapper.lt(false, s, DateUtil.parse(s3.toString()));
                        break;
                    case "tlte":
                        queryWrapper.lt(true, s, DateUtil.parse(s3.toString()));
                        break;
                    case "between":
                        try {
                            String v1 = StrUtil.trim(key.get(3).toString());
                            String v2 = StrUtil.trim(key.get(4).toString());
                            if (!StrUtil.hasBlank(v1, v2)) {
                                queryWrapper.between(false, s, DateUtil.parse(v1), DateUtil.parse(v2));
                            }
                        } catch (Throwable e) {
                            throw EasyException.wrap(BusCode.A000031,"query between values is error!");
                        }
                        break;
                    case "betweene":
                        try {
                            String v1 = StrUtil.trim(key.get(3).toString());
                            String v2 = StrUtil.trim(key.get(4).toString());
                            if (!StrUtil.hasBlank(v1, v2)) {
                                queryWrapper.between(true, s, DateUtil.parse(v1), DateUtil.parse(v2));
                            }
                        } catch (Throwable e) {
                            throw EasyException.wrap(BusCode.A000031,"query between values is error!");
                        }
                        break;
                    default:
                        throw EasyException.wrap(BusCode.A00047,s2);
                }
            }catch (Exception e){
                if(e instanceof EasyException){
                    throw e;
                }else{
                    logger.error("parsePageKeys has error ",e);
                }
            }

        }
    }
}
