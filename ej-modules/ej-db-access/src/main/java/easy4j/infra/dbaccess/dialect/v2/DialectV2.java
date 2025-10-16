package easy4j.infra.dbaccess.dialect.v2;

import easy4j.infra.dbaccess.helper.JdbcHelper;

public interface DialectV2 extends CrudDialect,SchemaMetaDialect {
    /**
     * 转义
     * @param name
     */
    String escape(String name);

    /**
     * 拆分转义
     * @param name
     * @param comma
     * @return
     */
    String splitEscape(String name,String comma);

    /**
     * 强制转义
     * @param name
     * @return
     */
    String forceEscape(String name);

    /**
     * 解转义
     * @param name
     */
    String unescape(String name);

    /**
     * 拆分解转义
     * @param name
     */
    String splitUnescape(String name,String comma);



    /**
     * 获取当前传入连接的数据库名称
     * @return
     */
    String getConnectionCatalog();

    /**
     * 获取当前传入连接的schema名称
     * @return
     */
    String getConnectionSchema();


    /**
     * 通过不同数据库来判断当前字段类型是否是 lob(大文本)类型
     *
     * @param typeName
     * @return
     */
    boolean isLob(String typeName);


    /**
     * 根据typeName确定javaclass的类型
     *
     * @param typeName
     * @return
     */
    Class<?> getJavaClassByTypeNameAndDbType(String typeName);


    /**
     * 通过不同数据库来判断当前字段类型是否是 json类型
     *
     * @param typeName
     * @return
     */
    boolean isJson(String typeName);


    /**
     * 字符串时间转为带函数的字符串
     * @param str
     * @return
     */
    String strConvertToDate(String str);

    /**
     * 获取默认时间 yyyy-MM-dd Hh24:mi:ss这种
     * @return
     */
    String getDefaultDateTime();

}
