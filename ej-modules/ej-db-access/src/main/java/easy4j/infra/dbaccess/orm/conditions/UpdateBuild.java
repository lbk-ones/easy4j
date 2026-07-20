package easy4j.infra.dbaccess.orm.conditions;


import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

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

    public static UpdateBuild get(){
        return new UpdateBuild();
    }

    public interface VoidFunc{
        void call();
    }
    
    
}
