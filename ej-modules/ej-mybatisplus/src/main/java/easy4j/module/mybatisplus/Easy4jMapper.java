package easy4j.module.mybatisplus;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

import java.util.List;
/**
 * 基础mapper封装
 * @author bokun.li
 * @date 2023/11/26
 */
public interface Easy4jMapper<T> extends BaseMapper<T> {

    int insertBatchSomeColumn(@Param("list") List<T> entityList);

    int alwaysUpdateSomeColumnById(@Param(Constants.ENTITY) T entity);
}
