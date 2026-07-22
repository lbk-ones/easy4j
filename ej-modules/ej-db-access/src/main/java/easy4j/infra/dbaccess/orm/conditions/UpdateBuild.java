package easy4j.infra.dbaccess.orm.conditions;


import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import easy4j.infra.common.utils.ListTs;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class UpdateBuild extends WhereBuild {

    public UpdateBuild updateBuild = this;

    @Getter
    private final List<String> sqlSet = new LinkedList<>();

    @Getter
    private final List<Object> args = new LinkedList<>();

    @Setter
    private boolean toUnderline = true;

    public String formatName(String name){
        String column_;
        if (toUnderline) {
            column_ = StrUtil.toUnderlineCase(name);
        } else {
            column_ = name;
        }
        return column_;
    }
    public UpdateBuild set(boolean condition, String column, String val) {

        return maybeDo(condition, () -> {
            sqlSet.add(formatName(column) + CompareOperator.EQUAL.getSymbol() + val);
        });
    }

    // col1 = ? + ? | arg1,arg2
    public UpdateBuild setSql(boolean condition, String setSql, Object... params) {
        return maybeDo(condition && StrUtil.isNotBlank(setSql), () -> {
            sqlSet.add(setSql);
            ListTs.addAll(args,ListTs.asList(params));
        });
    }

    
    public UpdateBuild setIncrBy(boolean condition, String column, Number val) {
        return maybeDo(condition, () -> {
            String formatName = formatName(column);
            sqlSet.add(String.format("%s=%s + %s", formatName, formatName, val instanceof BigDecimal ? ((BigDecimal) val).toPlainString() : val));
        });
    }

    
    public UpdateBuild setDecrBy(boolean condition, String column, Number val) {
        return maybeDo(condition, () -> {
            String formatName = formatName(column);
            sqlSet.add(String.format("%s=%s - %s", formatName, formatName, val instanceof BigDecimal ? ((BigDecimal) val).toPlainString() : val));
        });
    }

    public UpdateBuild maybeDo(boolean condition, VoidFunc voidFunc0){
        if(condition && voidFunc0!=null){
            voidFunc0.call();
        }
        return updateBuild;
    }


    // 基础比较条件方法
    public UpdateBuild eq(String column, Object value) {
        super.eq(column,value);
        return this;
    }

    public UpdateBuild eq(boolean option, String column, Object value) {
        super.eq(option,column,value);
        return this;
    }

    public UpdateBuild ne(String column, Object value) {
        super.ne(column,value);
        return this;
    }

    public UpdateBuild ne(boolean option, String column, Object value) {
        super.ne(option,column,value);
        return this;
    }

    public UpdateBuild gt(String column, Object value) {
        super.gt(column,value);
        return this;
    }

    public UpdateBuild gt(boolean option, String column, Object value) {
        super.gt(option,column,value);

        return this;
    }

    public UpdateBuild lt(String column, Object value) {
        super.lt(column,value);
        return this;
    }

    public UpdateBuild lt(boolean option, String column, Object value) {
        super.lt(option,column,value);
        return this;
    }

    public UpdateBuild gte(String column, Object value) {
        super.gte(column,value);
        return this;
    }

    public UpdateBuild gte(boolean option, String column, Object value) {
        super.gte(option,column,value);
        return this;
    }

    public UpdateBuild lte(String column, Object value) {
        super.lte(column,value);
        return this;
    }

    public UpdateBuild lte(boolean option, String column, Object value) {
        super.lte(option,column,value);
        return this;
    }

    // LIKE 条件
    public UpdateBuild like(String column, String value) {
        super.like(column,value);
        return this;
    }

    public UpdateBuild like(boolean option, String column, String value) {
        super.like(option,column,value);
        return this;
    }

    public UpdateBuild likeLeft(String column, String value) {
        super.likeLeft(column,value);
        return this;
    }

    public UpdateBuild likeLeft(boolean option, String column, String value) {
        super.likeLeft(option,column,value);
        return this;
    }

    public UpdateBuild likeRight(String column, String value) {
        super.likeRight(column,value);
        return this;
    }

    public UpdateBuild likeRight(boolean option, String column, String value) {
        if (option) super.likeRight(column,value);
        return this;
    }

    public UpdateBuild notLike(String column, String value) {
        super.notLike(column,value);
        return this;
    }

    public UpdateBuild notLike(boolean option, String column, String value) {
        super.notLike(option,column,value);
        return this;
    }

    // IN 条件
    public UpdateBuild in(String column, Collection<?> values) {
        super.in(column,values);
        return this;
    }

    public UpdateBuild in(boolean option, String column, Collection<?> values) {
        super.in(option,column,values);
        return this;
    }

    public UpdateBuild inArray(String column, Object... values) {
        super.inArray(column,values);
        return this;
    }

    public UpdateBuild inArray(boolean option, String column, Object... values) {
        super.inArray(option,column,values);
        return this;
    }

    public UpdateBuild notIn(String column, Collection<?> values) {
        super.notIn(column,values);
        return this;
    }

    public UpdateBuild notIn(boolean option, String column, Collection<?> values) {
        super.notIn(option,column,values);
        return this;
    }

    public UpdateBuild notIn(String column, Object... values) {
        super.notIn(column,values);
        return this;
    }

    public UpdateBuild notIn(boolean option, String column, Object... values) {
        super.notIn(option,column,values);
        return this;
    }

    // BETWEEN 条件
    public UpdateBuild between(String column, Object value1, Object value2) {
        super.between(column,value1,value2);
        return this;
    }

    public UpdateBuild between(boolean option, String column, Object value1, Object value2) {
        super.between(option,column,value1,value2);
        return this;
    }

    // NULL 条件
    @JsonIgnore
    public UpdateBuild isNull(String column) {
        super.isNull(column);
        return this;
    }

    @JsonIgnore
    public UpdateBuild isNull(boolean option, String column) {
        super.isNull(option,column);
        return this;
    }

    @JsonIgnore
    public UpdateBuild isNotNull(String column) {
        super.isNotNull(column);

        return this;
    }

    @JsonIgnore
    public UpdateBuild isNotNull(boolean option, String column) {
        super.isNotNull(option,column);

        return this;
    }


    public UpdateBuild sql(boolean option,String sql,Object ...args_){
        super.sql(option,sql,args_);

        return this;
    }
    public UpdateBuild sql(String sql,Object ...args_){
        super.sql(sql,args_);
        return this;
    }

    // 构建子条件
    public UpdateBuild and(WhereBuild subBuilder) {
        super.and(subBuilder);
        return this;
    }

    public UpdateBuild and(Consumer<WhereBuild> subBuilder) {
        super.and(subBuilder);
        return this;
    }

    public UpdateBuild or(WhereBuild subBuilder) {
        super.or(subBuilder);
        return this;
    }

    public UpdateBuild or(Consumer<WhereBuild> subBuilder) {
        super.or(subBuilder);
        return this;
    }

    public UpdateBuild not(WhereBuild subBuilder) {
        super.not(subBuilder);
        return this;
    }

    public UpdateBuild not(Consumer<WhereBuild> subBuilder) {
        super.not(subBuilder);
        return this;
    }
    
    

    public static UpdateBuild get(){
        return new UpdateBuild();
    }

    public interface VoidFunc{
        void call();
    }
    
    
}
