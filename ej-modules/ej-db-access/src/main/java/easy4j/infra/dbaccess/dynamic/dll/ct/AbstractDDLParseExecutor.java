package easy4j.infra.dbaccess.dynamic.dll.ct;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.CommonDBAccess;
import easy4j.infra.dbaccess.dynamic.dll.ct.table.DDLTableExecutor;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractDDLParseExecutor extends CommonDBAccess implements DDLParseExecutor {
    protected String getString(DDLTableExecutor dDlTableExecutor, List<String> objects) {
        String tableInfo = dDlTableExecutor.getTableInfo();
        List<String> comments = dDlTableExecutor.getComments();
        List<String> indexes = dDlTableExecutor.getIndexes();
        if (CollUtil.isNotEmpty(indexes)) {
            comments.addAll(indexes);
        }
        String join = String.join(",\n", objects);
        String ctTxt = MessageFormat.format(tableInfo, join);
        String collect = ListTs.asList(ctTxt, String.join(";\n", comments))
                .stream()
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.joining(";\n"));
        if (!StrUtil.endWith(collect, SP.SEMICOLON)) {
            collect += SP.SEMICOLON;
        }
        return collect;
    }

}
