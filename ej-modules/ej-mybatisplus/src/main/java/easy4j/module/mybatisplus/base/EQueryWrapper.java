package easy4j.module.mybatisplus.base;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * QueryWrapper 如果使用驼峰参数 并不会转下划线 明明配置是true 可能是不支持吧 不管了 围魏救赵一下
 * EQueryWrapper 主要解决上面那个问题 将参数转下划线
 *
 * @author bokun.li
 * @date 2025/8/15
 */
public class EQueryWrapper<T> extends QueryWrapper<T> {
    boolean camelToUnderLine = true;

    public void disabledCamelToUnderLine(){
        this.camelToUnderLine = false;
    }



    public EQueryWrapper() {
        this((T) null);
    }

    public EQueryWrapper(T entity) {
        super(entity);
    }

    public EQueryWrapper(Class<T> entityClass) {
        super(entityClass);
    }

    public EQueryWrapper(T entity, String... columns) {
        super(entity,columns);

    }


    @Override
    protected String columnToString(String column) {
        String s = super.columnToString(column);
        if(!camelToUnderLine) return s;
        return StrUtil.toUnderlineCase(s);
    }

}
