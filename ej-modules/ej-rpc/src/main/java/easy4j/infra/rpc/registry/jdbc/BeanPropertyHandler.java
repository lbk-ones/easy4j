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
package easy4j.infra.rpc.registry.jdbc;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.rpc.serializable.SerializableFactory;
import org.apache.commons.dbutils.handlers.AbstractListHandler;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 该类主要解决数据库字段与类属性之间的转换存在下划线的情况(如：taskId->task_id)
 * 该类主要参考Spring JDBC的rowmapper方式
 * dbutils使用BeanHandler、BeanListHandler来处理返回集与bean的转换
 * 这里统一使用BeanPropertyHandler，当返回单条记录时，使用JdbcHelper的requiredSingleResult做处理
 *
 * @param <T>
 */
public class BeanPropertyHandler<T> extends AbstractListHandler<T> {
    /**
     * 需要映射的bean对象的class类型
     */
    private Class<T> mappedClass;
    /**
     * 映射的字段
     */
    private Map<String, PropertyDescriptor> mappedFields;

    /**
     * 构造函数，根据bean对象的class类型初始化mappedFields
     *
     * @param mappedClass
     */
    public BeanPropertyHandler(Class<T> mappedClass) {
        initialize(mappedClass);
    }

    /**
     * ResultSet结果集处理
     */
    protected T handleRow(ResultSet rs) throws SQLException {
        /**
         * 根据bean的class类型实例化为对象
         */
        T mappedObject = ReflectUtil.newInstance(mappedClass);
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        /**
         * 对ResultSet结果集字段进行循环
         */
        for (int index = 1; index <= columnCount; index++) {
            /**
             * 根据字段索引index获取字段名称
             */
            String column = lookupColumnName(rsmd, index);
            /**
             * 根据映射字段集合返回字段名称对应的属性描述符对象
             */
            PropertyDescriptor pd = this.mappedFields.get(column.replaceAll(" ", "").toLowerCase());
            if (pd != null) {
                try {
                    /**
                     * 根据字段index、属性类型返回字段值
                     */
                    Class<?> propertyType = pd.getPropertyType();
                    Object value = getResultSetValue(rs, index, propertyType);

                    try {
                        /**
                         * 使用apache-beanutils设置对象的属性
                         */
                        BeanUtil.setProperty(mappedObject, pd.getName(), value);
                    } catch (Exception e) {
                        try {
                            // fix json type
                            if (isByteArray(value) && !propertyType.isAssignableFrom(byte[].class)) {
                                byte[] value1 = (byte[]) value;
                                String string = new String(value1, StandardCharsets.UTF_8);
                                String unescapedJson = string
                                        .replaceFirst("^\"", "")  // 移除开头的引号
                                        .replaceFirst("\"$", "")  // 移除结尾的引号
                                        .replace("\\\"", "\"");   // 替换 \" 为 "
                                if (!"NULL".equalsIgnoreCase(unescapedJson) & !"\"NULL\"".equalsIgnoreCase(unescapedJson)) {
                                    Object object = SerializableFactory.getJackson().deserializable(unescapedJson.getBytes(StandardCharsets.UTF_8), propertyType);
                                    ReflectUtil.setFieldValue(mappedObject, pd.getName(), object);
                                }
                            } else {
                                e.printStackTrace();
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return mappedObject;
    }

    public static boolean isByteArray(Object obj) {
        return obj instanceof byte[];
    }

    /**
     * 根据bean对象的class初始化字段映射集合
     *
     * @param mappedClass
     */
    protected void initialize(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
        this.mappedFields = new HashMap<>();
        Map<String, PropertyDescriptor> propertyDescriptorMap = BeanUtil.getPropertyDescriptorMap(mappedClass, true);
        for (PropertyDescriptor pd : propertyDescriptorMap.values()) {
            if (pd.getWriteMethod() != null) {
                this.mappedFields.put(pd.getName().toLowerCase(), pd);
                String underscoredName = StrUtil.toUnderlineCase(pd.getName());
                if (!pd.getName().toLowerCase().equals(underscoredName)) {
                    this.mappedFields.put(underscoredName, pd);
                }
            }
        }
    }

    /**
     * 属性名称转换为下划线，如taskId->task_id
     *
     * @param name
     * @return
     */
    private static String underscoreName(String name) {
        StringBuilder result = new StringBuilder();
        if (name != null && name.length() > 0) {
            result.append(name.substring(0, 1).toLowerCase());
            for (int i = 1; i < name.length(); i++) {
                String s = name.substring(i, i + 1);
                if (s.equals(s.toUpperCase())) {
                    result.append("_");
                    result.append(s.toLowerCase());
                } else {
                    result.append(s);
                }
            }
        }
        return result.toString();
    }

    /**
     * 由Introspector返回指定类型的BeanInfo对象，再返回需要的属性描述对象数组PropertyDescriptor[]
     *
     * @param c
     * @return PropertyDescriptor[]
     * @throws SQLException
     */
    private PropertyDescriptor[] propertyDescriptors(Class<?> c)
            throws SQLException {
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(c);
        } catch (IntrospectionException e) {
            throw new SQLException("Bean introspection failed: "
                    + e.getMessage());
        }

        return beanInfo.getPropertyDescriptors();
    }

    /**
     * 根据元数据ResultSetMetaData、列索引columIndex获取列名称
     *
     * @param resultSetMetaData
     * @param columnIndex
     * @return
     * @throws SQLException
     */
    public static String lookupColumnName(ResultSetMetaData resultSetMetaData, int columnIndex) throws SQLException {
        String name = resultSetMetaData.getColumnLabel(columnIndex);
        if (name == null || name.length() < 1) {
            name = resultSetMetaData.getColumnName(columnIndex);
        }
        return name;
    }


    /**
     * 根据ResultSet结果集、index列索引、字段类型requiredType获取指定类型的对象值
     *
     * @param rs
     * @param index
     * @param requiredType
     * @return
     * @throws SQLException
     */
    public static Object getResultSetValue(ResultSet rs, int index, Class<?> requiredType) throws SQLException {
        if (requiredType == null) {
            return getResultSetValue(rs, index);
        }

        Object value = null;
        boolean wasNullCheck = false;

        if (String.class.equals(requiredType)) {
            value = rs.getString(index);
        } else if (boolean.class.equals(requiredType) || Boolean.class.equals(requiredType)) {
            value = rs.getBoolean(index);
            wasNullCheck = true;
        } else if (byte.class.equals(requiredType) || Byte.class.equals(requiredType)) {
            value = rs.getByte(index);
            wasNullCheck = true;
        } else if (short.class.equals(requiredType) || Short.class.equals(requiredType)) {
            value = rs.getShort(index);
            wasNullCheck = true;
        } else if (int.class.equals(requiredType) || Integer.class.equals(requiredType)) {
            value = rs.getInt(index);
            wasNullCheck = true;
        } else if (long.class.equals(requiredType) || Long.class.equals(requiredType)) {
            value = rs.getLong(index);
            wasNullCheck = true;
        } else if (float.class.equals(requiredType) || Float.class.equals(requiredType)) {
            value = rs.getFloat(index);
            wasNullCheck = true;
        } else if (double.class.equals(requiredType) || Double.class.equals(requiredType) ||
                Number.class.equals(requiredType)) {
            value = rs.getDouble(index);
            wasNullCheck = true;
        } else if (byte[].class.equals(requiredType)) {
            value = rs.getBytes(index);
        } else if (Date.class.equals(requiredType)) {
            value = rs.getDate(index);
        } else if (Time.class.equals(requiredType)) {
            value = rs.getTime(index);
        } else if (Timestamp.class.equals(requiredType) || java.util.Date.class.equals(requiredType)) {
            value = rs.getTimestamp(index);
        } else if (BigDecimal.class.equals(requiredType)) {
            value = rs.getBigDecimal(index);
        } else if (Blob.class.equals(requiredType)) {
            value = rs.getBlob(index);
        } else if (Clob.class.equals(requiredType)) {
            value = rs.getClob(index);
        } else {
            value = getResultSetValue(rs, index);
        }

        if (wasNullCheck && rs.wasNull()) {
            value = null;
        }
        return value;
    }


    /**
     * 对于特殊字段类型做特殊处理
     *
     * @param rs
     * @param index
     * @return
     * @throws SQLException
     */
    public static Object getResultSetValue(ResultSet rs, int index) throws SQLException {
        Object obj = rs.getObject(index);
        String className = null;
        if (obj != null) {
            className = obj.getClass().getName();
        }
        if (obj instanceof Blob) {
            obj = rs.getBytes(index);
        } else if (obj instanceof Clob) {
            obj = rs.getString(index);
        } else if (className != null &&
                ("oracle.sql.TIMESTAMP".equals(className) ||
                        "oracle.sql.TIMESTAMPTZ".equals(className))) {
            obj = rs.getTimestamp(index);
        } else if (className != null && className.startsWith("oracle.sql.DATE")) {
            String metaDataClassName = rs.getMetaData().getColumnClassName(index);
            if ("java.sql.Timestamp".equals(metaDataClassName) ||
                    "oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
                obj = rs.getTimestamp(index);
            } else {
                obj = rs.getDate(index);
            }
        } else if (obj != null && obj instanceof Date) {
            if ("java.sql.Timestamp".equals(rs.getMetaData().getColumnClassName(index))) {
                obj = rs.getTimestamp(index);
            }
        }
        return obj;
    }

}
