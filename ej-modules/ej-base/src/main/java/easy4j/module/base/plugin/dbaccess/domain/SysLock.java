package easy4j.module.base.plugin.dbaccess.domain;

import easy4j.module.base.plugin.dbaccess.annotations.JdbcColumn;
import easy4j.module.base.plugin.dbaccess.annotations.JdbcTable;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 一个简单的系统锁
 *
 * @author bokun.li
 * @date 2025/5/29
 */
@Data
@JdbcTable(name = "sys_lock")
public class SysLock implements Serializable {

    @JdbcColumn(isPrimaryKey = true)
    private String id;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 过期时间
     */
    private Date expireDate;

    /**
     * 备注 这一次锁的详细信息
     * 比如说是谁成功抢走的
     * 或者说加锁具体内容
     */
    private String remark;
}
