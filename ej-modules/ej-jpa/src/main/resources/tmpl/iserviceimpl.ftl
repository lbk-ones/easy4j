package ${packageName};

<#list importList as imp>
import ${imp};
</#list>
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import easy4j.module.jpa.base.BaseEntity;
import easy4j.module.seed.CommonKey;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.persistence.Id;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import cn.hutool.core.collection.CollUtil;
import easy4j.module.base.utils.ListTs;
import easy4j.module.jpa.page.SortDto;

<#list lineList as line>
${line}
</#list>
<#list annotationList as anno>
@${anno}
</#list>
public class ${interfaceImplName} extends BaseService<${domainName}Dao,String,${domainName}> implements ${interfaceName} {
    public static Class<${domainName}> clazz = ${domainName}.class;

    @Override
    public Map<String,Object> get${domainName}List(${domainName}Dto ${firstLowDomainName}Dto) {
        isEmptyThrow(${firstLowDomainName}Dto, "A00004");
        Map<String,Object> res = Maps.newHashMap();
        List<SortDto> sortDtoList = ListTs.newArrayList();
        List<SortDto> ${firstLowDomainName}DtoSortDtoList = ${firstLowDomainName}Dto.getSortDtoList();
        if(CollUtil.isNotEmpty(${firstLowDomainName}DtoSortDtoList)){
            sortDtoList.addAll(${firstLowDomainName}DtoSortDtoList);
        }else{
            String ordStr = "";
            // from createTime
            if (BaseEntity.class.isAssignableFrom(clazz)) {
                ordStr = LambdaUtil.getFieldName(${domainName}::getCreateTime);
            }else{
                // get id
                Field[] fields = ReflectUtil.getFields(clazz, e -> e.isAnnotationPresent(Id.class));
                ordStr = fields[0].getName();
            }
            SortDto sortDto = new SortDto();
            sortDto.setOrderField(ordStr);
            sortDto.setOrderType("desc");
            sortDtoList.add(sortDto);
        }

        String searchKey = ${firstLowDomainName}Dto.getSearchKey();
        int recordStatus = ${firstLowDomainName}Dto.getRecordStatus() == 4?3:${firstLowDomainName}Dto.getRecordStatus();
        // filter
        Specification<${domainName}> specification = new Specification<${domainName}>() {

            @Override
            public Predicate toPredicate(Root<${domainName}> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                // TODO 查询条件待定
                List<Predicate> predicates = Lists.newArrayList();
                if(StrUtil.isNotBlank(searchKey)){
                    // like
                    Predicate name = cb.like(root.get("xx").as(String.class), like(searchKey));
                    // =
                    Predicate appCode = cb.equal(root.get("xx").as(String.class), searchKey);
                    predicates.add(cb.or(name,appCode));
                }
                // 启用的和禁用的都查出来
                if(recordStatus == 2){
                    predicates.add(cb.ge(root.get("isEnabled").as(Integer.class), 0));
                    // 全部
                }else if (recordStatus != 3){
                    // 指定查
                    predicates.add(cb.equal(root.get("isEnabled").as(Integer.class), recordStatus));
                }
                if(predicates.isEmpty()){
                    return null;
                }
                Predicate[] pre = new Predicate[predicates.size()];
                return cb.and(predicates.toArray(pre));
            }
        };
        Page<${domainName}Dto> byPage = this.findByPage(${firstLowDomainName}Dto, specification, ${domainName}Dto.class, sortDtoList.toArray(new SortDto[]{}));
        res.put("list",byPage.getContent());
        res.put("totals",byPage.getTotalElements());
        return res;
    }


    @Override
    @Transactional
    public List<${domainName}Dto> save${domainName}(List<${domainName}Dto> ${firstLowDomainName}Dtos) {
        isEmptyThrow(${firstLowDomainName}Dtos, "A00004");
        List<${domainName}Dto> res = new ArrayList<>();
        for (${domainName}Dto ${firstLowDomainName}Dto : ${firstLowDomainName}Dtos) {
            ${domainName} ${domainName} = new ${domainName}();
            ${firstLowDomainName}Dto.toNewEntityValidate();
            BeanUtil.copyProperties(${firstLowDomainName}Dto,${domainName});
            ${domainName}.setId(CommonKey.gennerString());
            ${domainName}.setVersion(1);
            ${domainName}.setIsEnabled(1);
            ${domainName}.setCreateTime(new Date());
            ${domainName}Dto ${firstLowDomainName}Dto1 = this.baseSaveReturnDto(${domainName}, ${firstLowDomainName}Dto);
            res.add(${firstLowDomainName}Dto1);
        }
        return res;
    }

    @Override
    @Transactional
    public List<${domainName}Dto> update${domainName}(List<${domainName}Dto> ${firstLowDomainName}Dtos) {
        isEmptyThrow(${firstLowDomainName}Dtos, "A00004");
        for (${domainName}Dto ${firstLowDomainName}Dto : ${firstLowDomainName}Dtos) {
            String dtoId = getDtoId(${firstLowDomainName}Dto);
            int version = getDtoVersion(${firstLowDomainName}Dto);
            ${domainName} ${firstLowDomainName} = this.updateById(${firstLowDomainName}Dto, dtoId, version);
            BeanUtil.copyProperties(${firstLowDomainName},${firstLowDomainName}Dto);
        }
        return ${firstLowDomainName}Dtos;
    }

    @Override
    @Transactional
    public List<String> delete${domainName}(List<${domainName}Dto> ${firstLowDomainName}Dtos) {
        isEmptyThrow(${firstLowDomainName}Dtos, "A00004");
        List<String> collect = ${firstLowDomainName}Dtos.stream().map(this::getDtoId).distinct().collect(Collectors.toList());
        boolean b = deleteByIds(collect, false);
        isTrueThrow(!b, "A00019");
        return collect;
    }

    @Override
    @Transactional
    public List<String> enableOrDisabled${domainName}(List<${domainName}Dto> ${firstLowDomainName}Dtos) {
        isEmptyThrow(${firstLowDomainName}Dtos, "A00004");
        List<String> collect = ${firstLowDomainName}Dtos.stream().map(this::getDtoId).distinct().collect(Collectors.toList());
        enableOrDisabled(collect);
        return collect;
    }
	
	@Override
    public List<${domainName}Dto> get${domainName}ByIds(List<String> ${firstLowDomainName}Ids) {
        List<${domainName}> allById = this.daoRepository.findAllById(${firstLowDomainName}Ids);
        if (CollUtil.isNotEmpty(allById)) {
            List<${domainName}Dto> ${firstLowDomainName}Dtos = new ArrayList<>();
            for (${domainName} ${firstLowDomainName} : allById) {
                ${domainName}Dto ${firstLowDomainName}Dto = new ${domainName}Dto();
                BeanUtil.copyProperties(${firstLowDomainName},${firstLowDomainName}Dto);
                ${firstLowDomainName}Dtos.add(${firstLowDomainName}Dto);
            }
            return ${firstLowDomainName}Dtos;
        }
        return null;
    }
}