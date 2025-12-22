package easy4j.module.mybatisplus.codegen;

import cn.hutool.core.bean.BeanUtil;
import easy4j.infra.common.utils.ListTs;
import lombok.Data;
@Data
public class MultiGenDto {

    GlobalGenConfig globalGenConfig;

    public MultiGenDto(GlobalGenConfig globalGenConfig) {
        this.globalGenConfig = globalGenConfig;
    }

    public static MultiGenDto build(GlobalGenConfig globalGenConfig_){
        return new MultiGenDto(globalGenConfig_);
    }

    /**
     * domainName-returnDtoName-cnDesc-entityName
     *
     * domainName 可以随意命名
     * returnDtoName 这个dto的名称必须存在
     * cnDesc 简短描述这个是啥
     * entityName 数据库实体类的名称 这个必须存在
     *
     * @param patterns 固定格式字符串
     * @return GenDto[]
     */
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
                    .setEntityName(entityName);
            BeanUtil.copyProperties(globalGenConfig,genDto);
            genDtos[i] = genDto;
            i++;
        }
        return genDtos;
    }
}
