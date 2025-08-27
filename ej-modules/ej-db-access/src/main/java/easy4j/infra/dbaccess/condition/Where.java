package easy4j.infra.dbaccess.condition;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 参数拼接器
 * <p>
 * Where.get("xxB1", "in", Where.wrapIn("0","1")) --> xx_b1 in ('0' , '1')
 * <p>
 * Where.get("xxB1", "=", "23") --> xx_b1  = 23
 * <p>
 * Where.getWrap("xxB1", "=", "23") --> xx_b1  = '23'
 *
 * @author bokun.li
 * @date 2025/8/27
 */
@Data
@Accessors(chain = true)
public class Where {

    private String fieldName;
    private String symbol = "";


    private Serializable[] arr;

    private boolean toUnderline = true;

    public String columnToStr(String fieldName) {
        if (toUnderline) {
            return StrUtil.toUnderlineCase(fieldName);
        }
        return fieldName;
    }

    public Where(@NotNull String fieldName, String symbol, Serializable... args) {
        this.fieldName = columnToStr(fieldName);
        this.symbol = symbol;

        List<Serializable> list = ListTs.asList(args);
        this.arr = list.toArray(new Serializable[]{});
    }

    public static String get(@NotNull String fieldName, String symbol, Serializable... args) {
        return new Where(fieldName, symbol, args).toString();
    }

    public static String getWrap(@NotNull String fieldName, String symbol, Serializable... args) {
        args = ListTs.map(ListTs.asList(args), e -> StrUtil.wrap(e.toString(), SP.SINGLE_QUOTE, SP.SINGLE_QUOTE)).toArray(new Serializable[]{});
        return new Where(fieldName, symbol, args).toString();
    }

    public static String getNotUnderlineStr(@NotNull String fieldName, String symbol, Serializable... args) {
        return new Where(fieldName, symbol, args).setToUnderline(false).toString();
    }

    @Override
    public String toString() {
        List<Serializable> list = ListTs.asList(fieldName);
        list.add(symbol);
        list.addAll(ListTs.asList(arr));
        List<String> map = ListTs.map(list, Convert::toStr);
        return String.join(SP.SPACE, map);
    }

    /**
     * 原样输出
     *
     * @param args
     * @return
     */
    public static WhereIn in(Serializable... args) {
        List<Serializable> list = ListTs.asList(args);
        WhereIn serializables = new WhereIn();
        serializables.addAll(list);
        return serializables;
    }

    /**
     * 用单引号包括起来
     *
     * @author bokun.li
     * @date 2025/8/27
     */
    public static WhereIn wrapIn(Serializable... args) {
        List<Serializable> list = ListTs.asList(args);
        list = ListTs.map(list, e -> StrUtil.wrap(e.toString(), SP.SINGLE_QUOTE, SP.SINGLE_QUOTE));
        WhereIn serializables = new WhereIn();
        serializables.addAll(list);
        return serializables;
    }

    public static class WhereIn extends ArrayList<Serializable> {
        @Override
        public String toString() {
            return "(" + stream().map(Object::toString).filter(StrUtil::isNotBlank).collect(Collectors.joining(SP.SPACE + SP.COMMA + SP.SPACE)) + ")";
        }
    }

}
