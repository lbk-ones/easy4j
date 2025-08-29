/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.mybatisplus.base;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateException;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlInjectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.context.api.user.UserContext;
import easy4j.infra.dbaccess.DBAccess;
import easy4j.infra.dbaccess.annotations.JdbcColumn;
import easy4j.module.mybatisplus.audit.AutoAudit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Id;
import java.lang.reflect.Field;
import java.util.*;

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
    public void parsePageKeysToQuery(APage pageDto, QueryWrapper<T> queryWrapper) {
        List<List<Object>> keys = pageDto.getKeys();
        parseKeysWith(queryWrapper, keys, true);
    }

    public void parsePageKeysToQuery(APage pageDto, QueryWrapper<T> queryWrapper, boolean toUnderLine) {
        List<List<Object>> keys = pageDto.getKeys();
        parseKeysWith(queryWrapper, keys, toUnderLine);
    }

    public void parseKeysToQuery(List<List<Object>> keys, QueryWrapper<T> queryWrapper) {
        parseKeysWith(queryWrapper, keys, true);
    }

    public void parseKeysToQuery(List<List<Object>> keys, QueryWrapper<T> queryWrapper, boolean toUnderLine) {
        parseKeysWith(queryWrapper, keys, toUnderLine);
    }

    private void parseKeysWith(QueryWrapper<T> queryWrapper, List<List<Object>> keys, boolean toUnderLine) {
        if (CollUtil.isEmpty(keys)) {
            return;
        }
        for (List<Object> key : keys) {
            try {
                String s = StrUtil.trim(key.get(0).toString());
                String s2 = StrUtil.trim(key.get(1).toString());
                Object s3 = key.get(2);
                if (StrUtil.hasBlank(s, s2) || null == s3) {
                    continue;
                }
                if (toUnderLine) {
                    s = StrUtil.toUnderlineCase(s);
                }
                if (SqlInjectionUtils.check(s)) {
                    throw new EasyException(BusCode.A00058);
                }
                switch (s2) {
                    case "eq":
                        queryWrapper.eq(s, s3);
                        break;
                    case "ne":
                        queryWrapper.ne(s, s3);
                        break;
                    case "in":
                        if (s3 instanceof Collection) {
                            Collection<?> s31 = (Collection<?>) s3;
                            queryWrapper.in(s, s31);
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
                            String v1 = StrUtil.trim(key.get(2).toString());
                            String v2 = StrUtil.trim(key.get(3).toString());
                            if (!StrUtil.hasBlank(v1, v2)) {
                                queryWrapper.between(false, s, DateUtil.parse(v1), DateUtil.parse(v2));
                            }
                        } catch (Throwable e) {
                            throw EasyException.wrap(BusCode.A000031, "query between values is error!");
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
                            throw EasyException.wrap(BusCode.A000031, "query between values is error!");
                        }
                        break;
                    default:
                        throw EasyException.wrap(BusCode.A00047, s2);
                }
            } catch (Exception e) {
                if (e instanceof EasyException) {
                    throw e;
                } else {
                    logger.error("parsePageKeys has error ", e);
                }
            }

        }
    }

    /**
     * 将 keys 转为 Map套Map 给 mybatis xml使用
     *
     * @param keys
     * @return
     * @see src/main/resources/example.xml
     */
    public Map<String, Map<String, Object>> parseKeysToMap(List<List<Object>> keys) {
        Map<String, Map<String, Object>> map = Maps.newHashMap();
        Map<String, Object> eqMap = Maps.newHashMap();
        Map<String, Object> neqMap = Maps.newHashMap();
        Map<String, Object> likeMap = Maps.newHashMap();
        Map<String, Object> likeLeftMap = Maps.newHashMap();
        Map<String, Object> likeRightMap = Maps.newHashMap();
        Map<String, Object> ltMap = Maps.newHashMap();
        Map<String, Object> lteMap = Maps.newHashMap();
        Map<String, Object> gtMap = Maps.newHashMap();
        Map<String, Object> gteMap = Maps.newHashMap();
        Map<String, Object> betweenMap = Maps.newHashMap();
        Map<String, Object> inMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(keys)) {
            try{
                for (List<Object> key : keys) {
                    Object name = ListTs.get(key, 0);
                    Object symbol = ListTs.get(key, 1);
                    Object value = ListTs.get(key, 2);
                    if (name != null && symbol != null && value != null) {
                        String columnName = name.toString();
                        columnName = StrUtil.toUnderlineCase(columnName);
                        if (SqlInjectionUtils.check(columnName)) {
                            throw new EasyException(BusCode.A00058);
                        }
                        String s2 = symbol.toString();
                        switch (s2) {
                            case "eq":
                                eqMap.put(columnName, value);
                                break;
                            case "ne":
                                neqMap.put(columnName, value);
                                break;
                            case "in":
                                inMap.put(columnName, value);
                                break;
                            case "like":
                                likeMap.put(columnName, value);
                                break;
                            case "likeLeft":
                                likeLeftMap.put(columnName, value);
                                break;
                            case "likeRight":
                                likeRightMap.put(columnName, value);
                                break;
                            case "lt":
                                ltMap.put(columnName, value);
                                break;
                            case "lte":
                                lteMap.put(columnName, value);
                                break;
                            case "gt":
                                gtMap.put(columnName, value);
                                break;
                            case "gte":
                                gteMap.put(columnName, value);
                                break;
                            case "tgt":
                                gtMap.put(columnName, DateUtil.parse(value.toString()));
                                break;
                            case "tgte":
                                gteMap.put(columnName, DateUtil.parse(value.toString()));
                                break;
                            case "tlt":
                                ltMap.put(columnName, DateUtil.parse(value.toString()));
                                break;
                            case "tlte":
                                lteMap.put(columnName, DateUtil.parse(value.toString()));
                                break;
                            case "between":
                                String v1 = StrUtil.trim(key.get(2).toString());
                                String v2 = StrUtil.trim(key.get(3).toString());
                                List<DateTime> list = ListTs.asList(DateUtil.parse(v1), DateUtil.parse(v2));
                                betweenMap.put(columnName, list);
                                break;
                            default:
                                throw EasyException.wrap(BusCode.A00047, s2);
                        }
                    }
                }
            }catch (DateException dateException){
                throw EasyException.wrap(BusCode.A00057, dateException.getMessage());
            }

        } else {
            return map;
        }
        map.put("eqMap", eqMap);
        map.put("neqMap", neqMap);
        map.put("likeMap", likeMap);
        map.put("likeLeftMap", likeLeftMap);
        map.put("likeRightMap", likeRightMap);
        map.put("ltMap", ltMap);
        map.put("lteMap", lteMap);
        map.put("gtMap", gtMap);
        map.put("gteMap", gteMap);
        map.put("betweenMap", betweenMap);
        map.put("inMap", inMap);

        return map;
    }

    public void copyObjIgnoreAudit(Object source, Object target) {
        if (null == source || null == target) return;
        String[] auditParams = getAuditParams();
        CopyOptions copyOptions = CopyOptions.create();
        copyOptions.ignoreNullValue();
        copyOptions.setIgnoreProperties(auditParams);
        BeanUtil.copyProperties(source, target, copyOptions);
    }

    public void clearAudit(Object source) {
        if (source == null) return;
        String[] auditParams = getAuditParams();
        for (String auditParam : auditParams) {
            ReflectUtil.setFieldValue(source, auditParam, null);
        }
    }

    protected static String[] getAuditParams() {
        List<String> objects = ListTs.newList();
        Field[] fields = ReflectUtil.getFields(AutoAudit.class);
        for (Field field : fields) {
            objects.add(field.getName());
        }
        return objects.toArray(new String[]{});
    }

    private DBAccess dbAccess;

    public DBAccess access() {
        if (dbAccess == null) {
            dbAccess = SpringUtil.getBean(DBAccess.class);
        }
        return dbAccess;
    }

    /**
     * 更新审计
     */
    public <A extends AutoAudit> void updateAudit(A autoAudit, Class<A> tClass, boolean updateDb) {
        if (null == autoAudit || tClass == null) return;
        UserContext userContext = getUserContext();
        if (null != userContext) {
            A a = ReflectUtil.newInstance(tClass);
            if (setId(autoAudit, a)) {
                return;
            }
            a.setUpdateBy(userContext.getUserName());
            a.setUpdateName(userContext.getUserNameCn());
            a.setLastUpdateTime(new Date());
            if (updateDb) {
                access().updateByPrimaryKeySelective(a, tClass, false);
            }
        }

    }

    /**
     * 更新审计 但是不跟新值
     */
    public <A extends AutoAudit> void updateAuditNoDb(A autoAudit, Class<A> tClass) {
        updateAudit(autoAudit, tClass, false);
    }

    /**
     * 更新审计 同时更新数据库
     * 数据库一定要有值
     */
    public <A extends AutoAudit> void updateAuditDb(A autoAudit, Class<A> tClass) {
        updateAudit(autoAudit, tClass, true);
    }

    /**
     * 写入主键的值
     *
     * @param from
     * @param to
     * @param <A>
     */
    public <A> boolean setId(A from, A to) {
        if (null == from || null == to) return true;
        Field[] fields = ReflectUtil.getFields(from.getClass(), e -> e.isAnnotationPresent(Id.class) || e.isAnnotationPresent(TableId.class) || (e.isAnnotationPresent(JdbcColumn.class) && e.getAnnotation(JdbcColumn.class).isPrimaryKey()));

        if (fields != null) {
            for (Field field : fields) {
                Object fieldValue = ReflectUtil.getFieldValue(from, field);
                ReflectUtil.setFieldValue(to, field, fieldValue);
            }
        }
        return fields == null || fields.length == 0;
    }

    /**
     * 写入审计
     */
    @Desc("写入审计 updateDb为true自动更新")
    public <A extends AutoAudit> void saveAudit(A autoAudit, Class<A> tClass, boolean updateDb) {
        if (null == autoAudit || tClass == null) return;
        UserContext userContext = getUserContext();
        if (null != userContext) {
            String userName = userContext.getUserName();
            String userNameCn = userContext.getUserNameCn();
            A a = ReflectUtil.newInstance(tClass);
            if (setId(autoAudit, a)) {
                return;
            }
            a.setCreateBy(userName);
            a.setCreateName(userNameCn);
            a.setCreateTime(new Date());
            a.setUpdateBy(userName);
            a.setUpdateName(userNameCn);
            a.setLastUpdateTime(new Date());
            if (updateDb) {
                access().updateByPrimaryKeySelective(a, tClass, false);
            }
        }
    }

    /**
     * 写入审计 但是不跟新值
     */
    @Desc("写入审计 但是不跟新值")
    public <A extends AutoAudit> void saveAuditNoDb(A autoAudit, Class<A> tClass) {
        saveAudit(autoAudit, tClass, false);
    }

    /**
     * 写入审计 同时更新数据库
     * 一定要在写入之后调用
     */
    @Desc("写入审计 同时更新数据库 调用时一定要保证数据库中有值")
    public <A extends AutoAudit> void saveAuditDb(A autoAudit, Class<A> tClass) {
        saveAudit(autoAudit, tClass, true);
    }

    public UserContext getUserContext() {
        Optional<Object> threadHashValue = Easy4j.getContext().getThreadHashValue(UserContext.USER_CONTEXT_NAME, UserContext.USER_CONTEXT_NAME);
        if (threadHashValue.isPresent()) {
            Object o = threadHashValue.get();
            return (UserContext) o;
        }
        return null;
    }

}
