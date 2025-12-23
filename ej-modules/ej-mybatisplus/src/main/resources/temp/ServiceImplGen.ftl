package ${parentPackageName}.${serviceImplPackageName};

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ${parentPackageName}.${controllerReqPackageName}.${domainName}ControllerReq;
import ${parentPackageName}.${entityPackageName}.${entityName};
import ${parentPackageName}.${dtoPackageName}.${entityName}Dto;
import ${parentPackageName}.${mapperPackageName}.${entityName}Mapper;
import ${parentPackageName}.${serviceInterfacePackageName}.I${domainName}Service;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.module.mybatisplus.base.BaseServiceImpl;
import easy4j.module.mybatisplus.base.EQueryWrapper;
import easy4j.module.mybatisplus.base.EasyPageRes;
import easy4j.module.mybatisplus.base.PageDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;

/**
 * ${headerDesc}
 * <p/>
 * @author ${author}
 * @since ${.now}
 */
@Service
public class ${domainName}ServiceImpl extends BaseServiceImpl<${entityName}Mapper, ${entityName}> implements I${domainName}Service {


    @Override
    public EasyPageRes pageQuery${domainName}(${domainName}ControllerReq req) {
        CheckUtils.checkByLambda(req, ${domainName}ControllerReq::getPageQuery);
        PageDto pageQuery = req.getPageQuery();
        List<List<Object>> keys = pageQuery.getKeys();
        EQueryWrapper<${entityName}> objectEQueryWrapper = new EQueryWrapper<>();
        parseKeysToQuery(keys, objectEQueryWrapper);
        objectEQueryWrapper.orderByDesc("create_time");
        Page<${entityName}> page = page(new Page<>(pageQuery.getPageNo(), pageQuery.getPageSize()));
        EasyPageRes from = EasyPageRes.from(page);
        List<${entityName}> records = from.getRecords(${entityName}.class);
        List<${entityName}Dto> flowProcDefDtos = list${entityName}ToDto(records);
        return from.setRecords(flowProcDefDtos);
    }

    public List<${entityName}Dto> list${entityName}ToDto(List<${entityName}> list) {
        return list.stream().map(flowProcDefDto -> {
            // TODO ${entityName} to ${entityName}Dto
            ${entityName}Dto flowProcDef = new ${entityName}Dto();
            BeanUtil.copyProperties(flowProcDefDto, flowProcDef);
            return flowProcDef;
        }).collect(Collectors.toList());
    }

    public List<${entityName}> list${entityName}DtoToDomain(List<${entityName}Dto> list) {
        // TODO ${entityName} to ${entityName}Dto
        return list.stream().map(e -> {
            ${entityName} flowProcDef = new ${entityName}();
            BeanUtil.copyProperties(e, flowProcDef);
            return flowProcDef;
        }).collect(Collectors.toList());
    }

    @Override
    public List<${entityName}Dto> getAllEnableNotDelete() {
        EQueryWrapper<${entityName}> query = new EQueryWrapper<>(${entityName}.class);
        boolean b = ReflectUtil.hasField(${entityName}.class, "isEnabled");
        boolean b2 = ReflectUtil.hasField(${entityName}.class, "isDeleted");
        if(b){
            query.eq("is_enabled", 1);
        }
        if(b2){
            query.eq("is_deleted", 0);
        }
        List<${entityName}> flowProcDefs = this.getBaseMapper().selectList(query);
        return list${entityName}ToDto(flowProcDefs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<${entityName}Dto> save${domainName}(${domainName}ControllerReq req) {
        CheckUtils.checkByLambda(req, ${domainName}ControllerReq::get${entityName}Dtos);
        List<${entityName}Dto> flowProcDefs = req.get${entityName}Dtos();
        List<${entityName}> newInsert = list${entityName}DtoToDomain(flowProcDefs);

        if (!newInsert.isEmpty()) {
            CheckUtils.checkInsert(saveBatch(newInsert));
        }
        return list${entityName}ToDto(newInsert);
    }


    @Override
    public List<${entityName}Dto> get${domainName}ByIds(List<String> strings) {
        if (ListTs.isEmpty(strings)) return new ArrayList<>();
        List<List<String>> partition = ListTs.partition(strings, 100);
        List<${entityName}> flowProcDefs = ListTs.newList();
        for (List<String> pList : partition) {
            EQueryWrapper<${entityName}> query = new EQueryWrapper<>(${entityName}.class);
            FieldInfo primaryKeyName = getPrimaryKeyName(${entityName}.class);
            List<Object> inList = convertPrimaryKey(pList, primaryKeyName);
            query.in(StrUtil.toUnderlineCase(primaryKeyName.getFieldName()), inList);
            query.eq("is_deleted", 0);
            List<${entityName}> queryList = this.getBaseMapper().selectList(query);
            ListTs.addAll(flowProcDefs, queryList);
        }
        return list${entityName}ToDto(flowProcDefs);
    }

    @Override
    public List<${entityName}Dto> publish${domainName}s(${domainName}ControllerReq req) {
        CheckUtils.checkByLambda(req,${domainName}ControllerReq::get${entityName}Dtos);
        // TODO
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<${entityName}Dto> delete${domainName}s(List<String> ids) {
        CheckUtils.checkParamNotNull(ids,"ids");
        List<${entityName}Dto> queryResList = get${domainName}ByIds(ids);
        if (ListTs.isNotEmpty(queryResList)) {
            boolean b = ReflectUtil.hasField(queryResList.get(0).getClass(), "isDeleted");
            if(b){
                for (${entityName}Dto dtoItem : queryResList) {
                    ReflectUtil.setFieldValue(dtoItem, "isDeleted",1);
                }
                ${domainName}ControllerReq req = new ${domainName}ControllerReq();
                req.set${entityName}Dtos(queryResList);
                return batchUpdate${domainName}(req);
            }else{
                for (${entityName}Dto item : queryResList) {
                    this.getBaseMapper().deleteById(item);
                }
            }
        }
        return ListTs.newList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<${entityName}Dto> batchUpdate${domainName}(${domainName}ControllerReq req) {
        CheckUtils.checkByLambda(req,${domainName}ControllerReq::get${entityName}Dtos);
        List<${entityName}Dto> flowProcDefDtos = req.get${entityName}Dtos();
        List<${entityName}> flowProcDefs = list${entityName}DtoToDomain(flowProcDefDtos);
        ${entityName}Mapper baseMapper1 = this.getBaseMapper();
        List<String> ids = ListTs.newList();
        for (${entityName} flowProcDef : flowProcDefs) {
            clearAudit(flowProcDef);
            baseMapper1.updateById(flowProcDef);
            String id = getIdValueToStr(flowProcDef);
            if(StrUtil.isNotBlank(id)){
                ids.add(id);
            }
        }
        return get${domainName}ByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<${entityName}Dto> copy${domainName}(${domainName}ControllerReq req) {
        CheckUtils.checkByLambda(req,${domainName}ControllerReq::get${entityName}Dtos);
        List<${entityName}Dto> flowProcDefDtos = req.get${entityName}Dtos();
        List<${entityName}> flowProcDefs = list${entityName}DtoToDomain(flowProcDefDtos);
        List<${entityName}> objects = new ArrayList<>();
        for (${entityName} flowProcDef : flowProcDefs) {
            clearId(flowProcDef);
            objects.add(flowProcDef);
        }
        req.set${entityName}Dtos(list${entityName}ToDto(objects));
        return save${domainName}(req);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<${entityName}Dto> enableOrDisable${domainName}(${domainName}ControllerReq req) {
        CheckUtils.checkByLambda(req,${domainName}ControllerReq::get${entityName}Dtos);
        List<${entityName}Dto> flowProcDefDtos = req.get${entityName}Dtos();
        List<String> collect = flowProcDefDtos.stream().map(this::getIdValueToStr).collect(Collectors.toList());
        List<${entityName}Dto> queryResList = get${domainName}ByIds(collect);
        if (ListTs.isNotEmpty(queryResList)) {
            boolean b = ReflectUtil.hasField(queryResList.get(0).getClass(), "isEnabled");
            if(b){
                for (${entityName}Dto item : queryResList) {
                    int isEnabled = Convert.toInt(ReflectUtil.getFieldValue(item, "isEnabled"));
                    if(isEnabled == 1){
                        ReflectUtil.setFieldValue(item, "isEnabled",0);
                    }else if(isEnabled == 0){
                        ReflectUtil.setFieldValue(item, "isEnabled",1);
                    }
                }
                ${domainName}ControllerReq newReq = new ${domainName}ControllerReq();
                newReq.set${entityName}Dtos(queryResList);
                return batchUpdate${domainName}(newReq);
            }
        }
        return ListTs.newList();
    }
}