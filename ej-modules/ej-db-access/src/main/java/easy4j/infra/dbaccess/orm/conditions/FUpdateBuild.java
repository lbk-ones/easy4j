package easy4j.infra.dbaccess.orm.conditions;


import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.function.Consumer;

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


    public FUpdateBuild<T> eq(Func1<T, ?> column, Object value) {
        super.eq(getName(column),value);
        return this;
    }

    public FUpdateBuild<T> eq(boolean option, Func1<T, ?> column, Object value) {
        super.eq(option,getName(column),value);
        return this;
    }

    public FUpdateBuild<T> ne(Func1<T, ?> column, Object value) {
        super.ne(getName(column),value);
        return this;
    }

    public FUpdateBuild<T> ne(boolean option, Func1<T, ?> column, Object value) {
        super.ne(option,getName(column),value);
        return this;
    }

    public FUpdateBuild<T> gt(Func1<T, ?> column, Object value) {
        super.gt(getName(column),value);
        return this;
    }

    public FUpdateBuild<T> gt(boolean option, Func1<T, ?> column, Object value) {
        super.gt(option,getName(column),value);

        return this;
    }

    public FUpdateBuild<T> lt(Func1<T, ?> column, Object value) {
        super.lt(getName(column),value);
        return this;
    }

    public FUpdateBuild<T> lt(boolean option, Func1<T, ?> column, Object value) {
        super.lt(option,getName(column),value);
        return this;
    }

    public FUpdateBuild<T> gte(Func1<T, ?> column, Object value) {
        super.gte(getName(column),value);
        return this;
    }

    public FUpdateBuild<T> gte(boolean option, Func1<T, ?> column, Object value) {
        super.gte(option,getName(column),value);
        return this;
    }

    public FUpdateBuild<T> lte(Func1<T, ?> column, Object value) {
        super.lte(getName(column),value);
        return this;
    }

    public FUpdateBuild<T> lte(boolean option, Func1<T, ?> column, Object value) {
        super.lte(option,getName(column),value);
        return this;
    }

    // LIKE 条件
    public FUpdateBuild<T> like(Func1<T, ?> column, String value) {
        super.like(getName(column),value);
        return this;
    }

    public FUpdateBuild<T> like(boolean option, Func1<T, ?> column, String value) {
        super.like(option,getName(column),value);
        return this;
    }

    public FUpdateBuild<T> likeLeft(Func1<T, ?> column, String value) {
        super.likeLeft(getName(column),value);
        return this;
    }

    public FUpdateBuild<T> likeLeft(boolean option, Func1<T, ?> column, String value) {
        super.likeLeft(option,getName(column),value);
        return this;
    }

    public FUpdateBuild<T> likeRight(Func1<T, ?> column, String value) {
        super.likeRight(getName(column),value);
        return this;
    }

    public FUpdateBuild<T> likeRight(boolean option, Func1<T, ?> column, String value) {
        if (option) super.likeRight(getName(column),value);
        return this;
    }

    public FUpdateBuild<T> notLike(Func1<T, ?> column, String value) {
        super.notLike(getName(column),value);
        return this;
    }

    public FUpdateBuild<T> notLike(boolean option, Func1<T, ?> column, String value) {
        super.notLike(option,getName(column),value);
        return this;
    }

    // IN 条件
    public FUpdateBuild<T> in(Func1<T, ?> column, Collection<?> values) {
        super.in(getName(column),values);
        return this;
    }

    public FUpdateBuild<T> in(boolean option, Func1<T, ?> column, Collection<?> values) {
        super.in(option,getName(column),values);
        return this;
    }

    public FUpdateBuild<T> inArray(Func1<T, ?> column, Object... values) {
        super.inArray(getName(column),values);
        return this;
    }

    public FUpdateBuild<T> inArray(boolean option, Func1<T, ?> column, Object... values) {
        super.inArray(option,getName(column),values);
        return this;
    }

    public FUpdateBuild<T> notIn(Func1<T, ?> column, Collection<?> values) {
        super.notIn(getName(column),values);
        return this;
    }

    public FUpdateBuild<T> notIn(boolean option, Func1<T, ?> column, Collection<?> values) {
        super.notIn(option,getName(column),values);
        return this;
    }

    public FUpdateBuild<T> notIn(Func1<T, ?> column, Object... values) {
        super.notIn(getName(column),values);
        return this;
    }

    public FUpdateBuild<T> notIn(boolean option, Func1<T, ?> column, Object... values) {
        super.notIn(option,getName(column),values);
        return this;
    }

    // BETWEEN 条件
    public FUpdateBuild<T> between(Func1<T, ?> column, Object value1, Object value2) {
        super.between(getName(column),value1,value2);
        return this;
    }

    public FUpdateBuild<T> between(boolean option, Func1<T, ?> column, Object value1, Object value2) {
        super.between(option,getName(column),value1,value2);
        return this;
    }

    // NULL 条件
    @JsonIgnore
    public FUpdateBuild<T> isNull(Func1<T, ?> column) {
        super.isNull(getName(column));
        return this;
    }

    @JsonIgnore
    public FUpdateBuild<T> isNull(boolean option, Func1<T, ?> column) {
        super.isNull(option,getName(column));
        return this;
    }

    @JsonIgnore
    public FUpdateBuild<T> isNotNull(Func1<T, ?> column) {
        super.isNotNull(getName(column));

        return this;
    }

    @JsonIgnore
    public FUpdateBuild<T> isNotNull(boolean option, Func1<T, ?> column) {
        super.isNotNull(option,getName(column));

        return this;
    }


    public FUpdateBuild<T> sql(boolean option,String sql,Object ...args_){
        super.sql(option,sql,args_);

        return this;
    }
    public FUpdateBuild<T> sql(String sql,Object ...args_){
        super.sql(sql,args_);
        return this;
    }

    // 构建子条件
    public FUpdateBuild<T> and(WhereBuild subBuilder) {
        super.and(subBuilder);
        return this;
    }

    public FUpdateBuild<T> and(Consumer<WhereBuild> subBuilder) {
        super.and(subBuilder);
        return this;
    }

    public FUpdateBuild<T> or(WhereBuild subBuilder) {
        super.or(subBuilder);
        return this;
    }

    public FUpdateBuild<T> or(Consumer<WhereBuild> subBuilder) {
        super.or(subBuilder);
        return this;
    }

    public FUpdateBuild<T> not(WhereBuild subBuilder) {
        super.not(subBuilder);
        return this;
    }

    public FUpdateBuild<T> not(Consumer<WhereBuild> subBuilder) {
        super.not(subBuilder);
        return this;
    }
    

    public static <T> FUpdateBuild<T> get(Class<T> aclass) {
        return new FUpdateBuild<>();
    }


}
