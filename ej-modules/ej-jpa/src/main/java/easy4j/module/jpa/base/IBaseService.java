package easy4j.module.jpa.base;


import easy4j.module.base.exception.EasyException;

import java.util.List;
import java.util.Map;


public interface IBaseService<T extends ControllerDto> {

    // 保存
    T save(T obj) throws EasyException;


    // 修改
    T update(T obj) throws EasyException;

    // 批量修改
    List<T> batchUpdate(T obj) throws EasyException;

    // 批量删除
    void deleteByIds(T obj) throws EasyException;

    // 页面刷新 分页
    Map<String,Object> refreshPage(PageParamsDto obj) throws EasyException;

    // 启用禁用
    String enableOrDisable(T obj) throws EasyException;

    // 根据ID获取已经启用了的
    T getById(T obj) throws EasyException;

}
