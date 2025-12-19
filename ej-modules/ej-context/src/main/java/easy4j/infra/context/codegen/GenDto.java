
package easy4j.infra.context.codegen;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.SP;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GenDto {

    // 实体名称 驼峰 帕斯卡命名发 必须是大写
    String domainName;

    // 中文描述
    String cnDesc;

    // 返回的dto名称
    String returnDtoName;

    // 父包名称
    String parentPackageName;

    // 项目所在绝对路径
    String projectAbsolutePath;

    // 模块英文名称
    String urlPrefix;

    // 数据库实体类名称
    String entityName;

    // 存在则删除
    boolean deleteIfExists;

    // 类文件头注释
    String headerDesc = "no desc";

    // 作者
    String author = "bokun.li";

    public GenDto setAuthor(String author) {
        if (author != null) {
            this.author = author;
        }
        return this;
    }

    public GenDto setDeleteIfExists(Boolean deleteIfExists) {
        if (deleteIfExists != null) {
            this.deleteIfExists = deleteIfExists;
        }
        return this;
    }

    public GenDto setUrlPrefix(String urlPrefix) {
        if (StrUtil.endWith(urlPrefix, SP.SLASH)) {
            urlPrefix = StrUtil.replaceLast(urlPrefix, SP.SLASH, "");
        }
        this.urlPrefix = urlPrefix;
        return this;
    }

    public GenDto setDomainName(String domainName) {
        this.domainName = StrUtil.upperFirst(domainName);
        return this;
    }


}
