package easy4j.infra.dbaccess.orm.conditions;


import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class FUpdateBuild<T> extends UpdateBuild {

    private String getName(Func1<T, ?> func) {
        return LambdaUtil.getFieldName(func);
    }

    public FUpdateBuild<T> updateBuild = this;

    public FUpdateBuild<T> set(boolean condition, Func1<T, ?> column, String val) {
        return this.maybeDo2(condition, () -> {
            String name = getName(column);
            super.set(condition, name, val);
        });
    }

    // col1 = ? + ?     arg1,arg2
    public FUpdateBuild<T> setSql(boolean condition, String setSql, Object... params) {

        return maybeDo2(condition && StrUtil.isNotBlank(setSql), () -> {
            super.setSql(condition, setSql, params);
        });
    }


    public FUpdateBuild<T> setIncrBy(boolean condition, Func1<T, ?> column, Number val) {
        return maybeDo2(condition, () -> {
            String name = getName(column);
            super.setIncrBy(condition, name, val);
        });
    }


    public FUpdateBuild<T> setDecrBy(boolean condition, Func1<T, ?> column, Number val) {
        return maybeDo2(condition, () -> {
            String name = getName(column);
            super.setDecrBy(condition, name, val);
        });
    }

    public FUpdateBuild<T> maybeDo2(boolean condition, VoidFunc voidFunc0) {
        if (condition && voidFunc0 != null) {
            voidFunc0.call();
        }
        return updateBuild;
    }

    public static <T> FUpdateBuild<T> get(Class<T> aclass) {
        return new FUpdateBuild<>();
    }


}
