package easy4j.infra.context.codegen;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MultiGenDto {

    // 父包名称
    String parentPackageName;

    // 项目所在绝对路径
    String projectAbsolutePath;

    // url前缀
    String urlPrefix;

    // 作者
    String author = null;

    Boolean deleteIfExists;


    public MultiGenDto setUrlPrefix(String urlPrefix) {
        if(StrUtil.endWith(urlPrefix, SP.SLASH)){
            urlPrefix = StrUtil.replaceLast(urlPrefix,SP.SLASH,"");
        }
        this.urlPrefix = urlPrefix;
        return this;
    }

    public static MultiGenDto build() {
        return new MultiGenDto();
    }

    // domainName-returnDtoName-cnDesc-entityName
    public GenDto[] multiGen(String... patterns) {
        GenDto[] genDtos = new GenDto[patterns.length];
        int i = 0;
        for (String pattern : patterns) {
            String[] split = pattern.split("-");
            String domainName = ListTs.get(split, 0);
            String returnDtoName = ListTs.get(split, 1);
            String cnDesc = ListTs.get(split, 2);
            String entityName = ListTs.get(split, 3);
            GenDto genDto = new GenDto()
                    .setDomainName(domainName)
                    .setReturnDtoName(returnDtoName)
                    .setCnDesc(cnDesc)
                    .setHeaderDesc(cnDesc)
                    .setEntityName(entityName)
                    .setAuthor(this.author)
                    .setDeleteIfExists(this.deleteIfExists)
                    .setUrlPrefix(this.urlPrefix)
                    .setParentPackageName(this.parentPackageName)
                    .setProjectAbsolutePath(this.projectAbsolutePath);
            genDtos[i] = genDto;
            i++;
        }
        return genDtos;
    }
}
