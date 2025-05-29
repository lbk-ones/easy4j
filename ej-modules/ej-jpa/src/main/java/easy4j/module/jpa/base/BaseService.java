/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.jpa.base;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.plugin.i18n.I18nBean;
import easy4j.module.jpa.page.PageableTools;
import easy4j.module.jpa.page.SortDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.annotation.Resource;
import javax.persistence.Id;
import javax.persistence.Version;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 将一些基本的操作放到baseService里面来
 * @author bokun.li
 * @date 2023/6/1
 */
public class BaseService<M extends JpaRepository<Entity,ID> & JpaSpecificationExecutor<Entity>,ID,Entity extends BaseEntity> {

    @Autowired
    protected M daoRepository;

    /**
     * 一个基本的保存操作
     * @author bokun.li
     * @date 2023/6/3
     */
    public <DTO extends BaseDto> DTO baseSaveReturnDto(Entity modal,DTO reDto){
        Entity save = this.daoRepository.save(modal);
        BeanUtil.copyProperties(save,reDto);
        return reDto;
    }
    public <DTO extends BaseDto> DTO copyToNewDto(Entity entity, Class<DTO> dtoClass){
        DTO dto = ReflectUtil.newInstance(dtoClass);
        BeanUtil.copyProperties(entity,dto);
        return dto;
    }
    public <DTO extends BaseDto> DTO copyToDto(Entity entity, DTO dto){
        BeanUtil.copyProperties(entity,dto);
        return dto;
    }
    public Entity baseSave(Entity modal){
        return this.daoRepository.save(modal);
    }
    /**
     * 启用或者禁用（通用类型）
     * @author bokun.li
     * @date 2023/6/1
     */
    public String enableOrDisabled(List<ID> id) throws EasyException {
        String wt= I18nBean.getMessage("A00001");
        List<Entity> allById = daoRepository.findAllById(id);
        boolean isSingle = false;
        if(CollUtil.isNotEmpty(allById) && allById.size()==1){
            isSingle = true;
        }
        for (Entity t : allById) {
            int isEnabled = t.getIsEnabled();
            if(isEnabled == -1){
                throw new EasyException("A00012");
            }
            if(isEnabled == 1){
                t.setIsEnabled(0);
                if(isSingle){
                    wt = "A00010";
                }
            }else if(isEnabled == 0){
                t.setIsEnabled(1);
                if(isSingle){
                    wt = "A00011";
                }
            }
        }
        daoRepository.saveAll(allById);
        return wt;
    }

    /**
     * 有选择性的更新
     * @author bokun.li
     * @date 2023/6/1
     */
    public <DTO extends BaseDto> Entity updateBySelective(DTO r,ID id,int version) throws EasyException{
        Optional<Entity> byId = daoRepository.findById(id);
        Entity save;
        if (byId.isPresent()) {
            Entity t = byId.get();
            int isEnabled = t.getIsEnabled();
            if(isEnabled == -1){
                throw new EasyException("A00012");
            }
            int version1 = t.getVersion();
            if (r.checkIsModify(version1,version)) {
                throw new EasyException("A00008");
            }
            r.copyPickPropertyToOtherObjToUpdate(t);
            version1++;
            t.setVersion(version1);
            save = daoRepository.save(t);
        }else{
            throw new EasyException("A00009");
        }
        return save;
    }

    public <DTO extends BaseDto> Entity updateById(DTO r,ID id,int version) throws EasyException{
        Optional<Entity> byId = daoRepository.findById(id);
        Entity save;
        if (byId.isPresent()) {
            Entity t = byId.get();
            int isEnabled = t.getIsEnabled();
            if(isEnabled == -1){
                throw new EasyException("A00012");
            }
            int version1 = t.getVersion();
            if (r.checkIsModify(version1,version)) {
                throw new EasyException("A00008");
            }
            BeanUtil.copyProperties(r,t);
            version1++;
            t.setVersion(version1);
            t.setId((String) id);
            save = daoRepository.save(t);
        }else{
            throw new EasyException("A00009");
        }
        return save;
    }

    /**
     * 批量删除
     * @author bokun.li
     * @date 2023/6/1
     */
    public boolean deleteByIds(List<ID> ids, boolean isDeleteDb) throws EasyException{
        boolean isSuccess = true;
        try{
            if(CollUtil.isEmpty(ids)){
                return isSuccess;
            }
            // 真删除
            if(isDeleteDb){
                daoRepository.deleteAllById(ids);
            }else{
                // 假删除
                List<Entity> allById = daoRepository.findAllById(ids);
                if(CollUtil.isEmpty(allById)){
                    throw new EasyException("A00012");
                }
                for (Entity t : allById) {
                    int isEnabled = t.getIsEnabled();
                    if(isEnabled == -1){
                        throw new EasyException("A00020");
                    }
                    t.setIsEnabled(-1);
                }
                List<Entity> ts = daoRepository.saveAll(allById);
            }
        }catch (Exception e){
            isSuccess = false;
        }
        return isSuccess;
    }

    /**
     * 默认以 creatTime 倒序排序
     * @author bokun.li
     * @date 2023/6/1
     */
    public <DTO extends BaseDto> Page<DTO>  findByPage(DTO pageParamsDto, Specification<Entity> jpaSpecification,Class<DTO> dtoClass) throws EasyException {
        Integer pageNo = pageParamsDto.getPageNo();
        Integer pageSize = pageParamsDto.getPageSize();
        Pageable pageOf = PageableTools.basicPage(pageNo, pageSize, "createTime");
        Page<Entity> all = daoRepository.findAll(jpaSpecification, pageOf);
        List<Entity> content = all.getContent();
        List<DTO> rs = BeanUtil.copyToList(content, dtoClass);
        return new PageImpl<>(rs, all.getPageable(), all.getTotalElements());
    }

    /**
     * 一个排序字段 倒序
     * @author bokun.li
     * @date 2023/6/2
     */
    public <DTO extends BaseDto> Page<DTO>  findByPage(DTO pageParamsDto, Specification<Entity> jpaSpecification,Class<DTO> dtoClass,String orderField) throws EasyException {
        Integer pageNo = pageParamsDto.getPageNo();
        Integer pageSize = pageParamsDto.getPageSize();
        Pageable pageOf = PageableTools.basicPage(pageNo, pageSize, orderField);
        Page<Entity> all = daoRepository.findAll(jpaSpecification, pageOf);
        List<Entity> content = all.getContent();
        List<DTO> rs = BeanUtil.copyToList(content, dtoClass);
        return new PageImpl<>(rs, all.getPageable(), all.getTotalElements());
    }

    /**
     * 自定义排序字段
     * @author bokun.li
     * @date 2023/6/2
     */
    public <DTO extends BaseDto> Page<DTO>  findByPage(DTO pageParamsDto, Specification<Entity> jpaSpecification, Class<DTO> dtoClass, SortDto...sortDtos) throws EasyException {
        Integer pageNo = pageParamsDto.getPageNo();
        Integer pageSize = pageParamsDto.getPageSize();
        Pageable pageOf = PageableTools.basicPage(pageNo, pageSize, sortDtos);
        Page<Entity> all = daoRepository.findAll(jpaSpecification, pageOf);
        List<Entity> content = all.getContent();
        List<DTO> rs = BeanUtil.copyToList(content, dtoClass);
        return new PageImpl<>(rs, all.getPageable(), all.getTotalElements());
    }

    /**
     * 根据ID获取 已经启用的应用
     * @author bokun.li
     * @date 2023/6/2
     */
    public  Entity getEnableOrDisableById(JpaRepository<Entity,ID> jpaRepository, ID id) throws EasyException {
        Optional<Entity> byId = jpaRepository.findById(id);
        if (byId.isPresent()) {
            Entity t1 = byId.get();
            int isEnabled = t1.getIsEnabled();
            if(isEnabled == 1 || isEnabled == 0){
                return t1;
            }else{
                throw new EasyException("A00012");
            }
        }else {
            throw new EasyException("A00013");
        }

    }

    public <DTO extends BaseDto> String getDtoId(DTO dto){
        Field[] fields = ReflectUtil.getFields(dto.getClass(), e -> e.isAnnotationPresent(Id.class));
        if(fields.length == 0){
            throw new EasyException("A00014");
        }else{
            Field field = fields[0];
            String fieldName = ReflectUtil.getFieldName(field);
            Object fieldValue = ReflectUtil.getFieldValue(dto, fieldName);
            if (fieldValue == null){
                throw new EasyException("A00015");
            }
            return fieldValue.toString();
        }
    }

    public <DTO extends BaseDto> int getDtoVersion(DTO dto){
        Field[] fields = ReflectUtil.getFields(dto.getClass(), e -> e.isAnnotationPresent(Version.class));
        if(fields.length == 0){
            throw new EasyException("A00016");
        }else{
            Field field = fields[0];
            String fieldName = ReflectUtil.getFieldName(field);
            Object fieldValue = ReflectUtil.getFieldValue(dto, fieldName);
            if(fieldValue == null){
                throw new EasyException("A00017");
            }
            String string = fieldValue.toString();
            if (StrUtil.isNumeric(string)) {
                return Integer.parseInt(string);
            }else{
                throw new EasyException("A00018");
            }
        }
    }


    public String like(String searchKey) {
        if (StrUtil.isNotEmpty(searchKey)) {
            return "%" + searchKey + "%";
        }
        return "";
    }

    public void isEmptyThrow(Object object,String thr){
        if(object == null){
            throw new EasyException(thr);
        }else{
            if(object instanceof Collection){
                Collection<?> object1 = (Collection<?>) object;
                if (object1.isEmpty()) {
                    throw new EasyException(thr);
                }
            }else if (object instanceof Map){
                Map<?, ?> object1 = (Map<?, ?>) object;
                if (object1.isEmpty()) {
                    throw new EasyException(thr);
                }
            }else if(StrUtil.isBlankIfStr(object)){
                throw new EasyException(thr);
            }
        }

    }

    public void isTrueThrow(boolean b,String thr){
        if (b){
            throw new EasyException(thr);
        }

    }
}
