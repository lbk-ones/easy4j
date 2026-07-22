package easy4j.infra.dbaccess.orm;

public enum OperateType {

    // 查询
    SELECT,
    // 分页查询
    SELECT_PAGE,
    // 是否存在
    SELECT_EXIST,
    // 查询数量
    SELECT_COUNT,
    // 更新
    UPDATE,
    // 写入
    INSERT,
    // 删除
    DELETE,
    // TRUNCATE操作
    TRUNCATE
}
