package easy4j.infra.dbaccess.orm;

import lombok.Getter;

/**
 * 查询统一以 SELECT_开头
 * 更新统一以 UPDATE_开头
 * 删除统一以 DELETE_开头
 * 写入统一以 INSERT_开头
 * 其他无所谓
 */
@Getter
public enum OperateType {

    // 查询
    SELECT(true, false, false, false),
    // 分页查询
    SELECT_PAGE(true, false, false, false),
    // 是否存在
    SELECT_EXIST(true, false, false, false),
    // 查询数量
    SELECT_COUNT(true, false, false, false),
    // 带join的查询
    SELECT_JOIN(true, false, false, false),
    // 更新
    UPDATE(false, true, false, false),
    // 写入
    INSERT(false, false, true, false),
    // 删除
    DELETE(false, false, false, true),
    // TRUNCATE操作
    TRUNCATE(false, false, false, true);
    private final boolean isSelect;
    private final boolean isUpdate;
    private final boolean isSave;
    private final boolean isDelete;

    OperateType(boolean isSelect, boolean isUpdate, boolean isSave, boolean isDelete) {
        this.isSelect = isSelect;
        this.isUpdate = isUpdate;
        this.isSave = isSave;
        this.isDelete = isDelete;
    }
}
