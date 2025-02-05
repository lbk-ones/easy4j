package easy4j.module.jpa.base;

import easy4j.module.base.exception.EasyException;
import easy4j.module.base.header.EasyResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
/**
 * 一个基本的crud模型 如果某个业务需要基础的crud那么就可以实现这些接口 so good...
 * @authoEasyResult<?> bokun.li
 * @date 2023/6/2
 */
public interface CrudController<T extends ControllerDto> {
    /**
     * 保存 单个
     * @authoEasyResult<?> bokun.li
     * @date 2023/6/2
     */
    @PostMapping("save")
    EasyResult<?> save(@RequestBody T object) throws EasyException;
    /**
     * 批量删除 单体删除
     * @authoEasyResult<?> bokun.li
     * @date 2023/6/2
     */
    @PostMapping("deleteByIds")
    EasyResult<?> deleteByIds(@RequestBody T object) throws EasyException;

    /**
     * 更新
     * @authoEasyResult<?> bokun.li
     * @date 2023/6/2
     */
    @PostMapping("update")
    EasyResult<?> update(@RequestBody T object) throws EasyException;

    /**
     * 批量更新
     * @authoEasyResult<?> bokun.li
     * @date 2023/6/2
     */
    @PostMapping("batchUpdate")
    EasyResult<?> batchUpdate(@RequestBody T object) throws EasyException;

    /**
     * 分页查询
     * @authoEasyResult<?> bokun.li
     * @date 2023/6/2
     */
    @PostMapping("refreshPage")
    EasyResult<?> refreshPage(@RequestBody PageParamsDto object) throws EasyException;

    /**
     * 启用禁用
     * @authoEasyResult<?> bokun.li
     * @date 2023/6/2
     */
    @PostMapping("enable")
    EasyResult<?> enable(@RequestBody T object) throws EasyException;

    /**
     * 通过ID来获取
     * @authoEasyResult<?> bokun.li
     * @date 2023/6/2
     */
    @PostMapping("getById")
    EasyResult<?> getById(@RequestBody T object) throws EasyException;




}
