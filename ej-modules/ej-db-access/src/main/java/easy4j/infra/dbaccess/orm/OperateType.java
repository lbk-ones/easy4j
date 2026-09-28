package easy4j.infra.dbaccess.orm;

import easy4j.infra.dbaccess.orm.runner.SqlRunnerTypeEnum;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

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
    SELECT(true, false, false, false, SqlRunnerTypeEnum.select),
    // 分页查询
    SELECT_PAGE(true, false, false, false, SqlRunnerTypeEnum.select),
    // 是否存在,
    SELECT_EXIST(true, false, false, false, SqlRunnerTypeEnum.exist),
    // 查询数量
    SELECT_COUNT(true, false, false, false, SqlRunnerTypeEnum.count),
    // join分页
    SELECT_JOIN_PAGE(true, false, false, false, SqlRunnerTypeEnum.select),
    // join count
    SELECT_JOIN_COUNT(true, false, false, false, SqlRunnerTypeEnum.count),
    // 带join的查询
    SELECT_JOIN(true, false, false, false, SqlRunnerTypeEnum.select),
    // 更新
    UPDATE(false, true, false, false, SqlRunnerTypeEnum.other),
    // 写入
    INSERT(false, false, true, false, SqlRunnerTypeEnum.other),
    // 删除
    DELETE(false, false, false, true, SqlRunnerTypeEnum.other),
    // TRUNCATE操作
    TRUNCATE(false, false, false, true, SqlRunnerTypeEnum.other);

    /**
     * 是否是查询类操作
     */
    private final boolean isSelect;

    /**
     * 是否是更新类操作
     */
    private final boolean isUpdate;

    /**
     * 是否是插入类操作
     */
    private final boolean isSave;

    /**
     * 是否是删除类操作
     */
    private final boolean isDelete;

    /**
     * sqlrunner的操作类型
     * ps执行的类型
     */
    private final SqlRunnerTypeEnum runnerType;

    OperateType(boolean isSelect, boolean isUpdate, boolean isSave, boolean isDelete, SqlRunnerTypeEnum runnerType) {
        this.isSelect = isSelect;
        this.isUpdate = isUpdate;
        this.isSave = isSave;
        this.isDelete = isDelete;
        this.runnerType = runnerType;
    }


    public static List<OperateType> getOperateTypeByRunnerType(SqlRunnerTypeEnum sqlRunnerTypeEnum) {
        List<OperateType> list = new ArrayList<>();
        OperateType[] values = OperateType.values();
        for (OperateType value : values) {
            SqlRunnerTypeEnum runnerType1 = value.getRunnerType();
            if (sqlRunnerTypeEnum == runnerType1) {
                list.add(value);
            }
        }
        return list;
    }

    /**
     * @return 返回所有查询类型的操作
     * @see RuntimeContext#getArgs()
     */
    public static List<OperateType> getSelectOperateTypes() {
        List<OperateType> list = new ArrayList<>();
        OperateType[] values = OperateType.values();
        for (OperateType value : values) {
            if (value.isSelect()) {
                list.add(value);
            }
        }
        return list;
    }

    /**
     * @return 返回所有查询类型的操作
     * @see RuntimeContext#getArgs()
     */
    public static List<OperateType> getInsertOperateTypes() {
        List<OperateType> list = new ArrayList<>();
        OperateType[] values = OperateType.values();
        for (OperateType value : values) {
            if (value.isSave()) {
                list.add(value);
            }
        }
        return list;
    }

    /**
     * @return 返回所有查询类型的操作
     * @see RuntimeContext#getArgs()
     */
    public static List<OperateType> getUpdateOperateTypes() {
        List<OperateType> list = new ArrayList<>();
        OperateType[] values = OperateType.values();
        for (OperateType value : values) {
            if (value.isUpdate()) {
                list.add(value);
            }
        }
        return list;
    }

    /**
     * @return 返回所有查询类型的操作
     * @see RuntimeContext#getArgs()
     */
    public static List<OperateType> getDeleteOperateTypes() {
        List<OperateType> list = new ArrayList<>();
        OperateType[] values = OperateType.values();
        for (OperateType value : values) {
            if (value == TRUNCATE) {
                continue;
            }
            if (value.isDelete()) {
                list.add(value);
            }
        }
        return list;
    }
}
