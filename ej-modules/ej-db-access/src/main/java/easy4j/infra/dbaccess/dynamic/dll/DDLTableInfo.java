package easy4j.infra.dbaccess.dynamic.dll;

import easy4j.infra.common.annotations.Desc;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndexInfo;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Index;
import java.util.List;

@Data
@Accessors(chain = true)
public class DDLTableInfo {

    @Desc("schema信息")
    private String schema;

    @Desc("数据库类型")
    private String dbType;

    @Desc("数据库版本")
    private String dbVersion;

    @Desc("表名")
    private String tableName;

    @Desc("数据库引擎 只支持mysql")
    private String engine;

    @Desc("字符集（支持emoji）只支持mysql")
    private String charset;

    @Desc("排序规则 只支持mysql")
    private String collate;

    @Desc("是否存在，只支持mysql 和 pg")
    private boolean ifNotExists;

    @Desc("是否临时表")
    private boolean isTemporary;

    @Desc("pg数据库，unlogged 表")
    private boolean pgUnlogged;

    @Desc("pg 继承表")
    private String pgInherits;

    @Desc("表名注释")
    private String comment;

    @Desc("domain class对象")
    private Class<?> domainClass;

    @Desc("字段信息列表")
    private List<DDLFieldInfo> fieldInfoList;

    @Desc("索引信息")
    private List<DDLIndexInfo> ddlIndexInfoList;

    @Desc("配置上下文信息")
    private DDLConfig dllConfig;

}
