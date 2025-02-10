package easy4j.module.jpa.base;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.module.base.exception.EasyException;
import easy4j.module.jpa.page.PageableTools;
import easy4j.module.jpa.page.SortDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.persistence.Id;
import javax.persistence.Version;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 将一些基本的操作放到baseService里面来
 * @author bokun.li
 * @date 2023/6/1
 */
public class BaseService {

    /**
     * 一个基本的保存操作
     * @author bokun.li
     * @date 2023/6/3
     */
    public <T extends BaseDto,R extends BaseEntity> T baseSave(JpaRepository<R,String> jpaRepository,T obj,R modal){
        R save = jpaRepository.save(modal);
        Class<T> aClass = (Class<T>) obj.getClass();
        T baseDto = ReflectUtil.newInstance(aClass);
        BeanUtil.copyProperties(save,baseDto);
        return baseDto;
    }
    /**
     * 启用或者禁用（通用类型）
     * @author bokun.li
     * @date 2023/6/1
     */
    public <T extends BaseEntity,R extends BaseDto,N> String enableOrDisabled(JpaRepository<T,N> jpaRepository,List<N> id) throws EasyException {
        String wt="操作成功";
        List<T> allById = jpaRepository.findAllById(id);
        boolean isSingle = false;
        if(CollUtil.isNotEmpty(allById) && allById.size()==1){
            isSingle = true;
        }
        for (T t : allById) {
            int isEnabled = t.getIsEnabled();
            if(isEnabled == 1){
                t.setIsEnabled(0);
                if(isSingle){
                    wt = "禁用成功";
                }
            }else if(isEnabled == 0){
                t.setIsEnabled(1);
                if(isSingle){
                    wt = "启用成功";
                }
            }
        }
        jpaRepository.saveAll(allById);
        return wt;
    }

    /**
     * 有选择性的更新
     * @author bokun.li
     * @date 2023/6/1
     */
    public <T extends BaseEntity,R extends BaseDto,N> T updateBySelective(JpaRepository<T,N> jpaRepository,R r,N id,int version) throws EasyException{
        Optional<T> byId = jpaRepository.findById(id);
        String wt = "";
        T save = null;
        if (byId.isPresent()) {
            T t = byId.get();
            if (r.checkIsModify(t.getVersion(),version)) {
                throw new EasyException("数据已变更请再次提交");
            }
            r.copyPickPropertyToOtherObj(t);
            save = jpaRepository.save(t);
        }else{
            throw new EasyException("没有可以更新的记录");
        }
        return save;
    }
    /**
     * 批量删除
     * @author bokun.li
     * @date 2023/6/1
     */
    public <T extends BaseEntity,R> boolean deleteByIds(JpaRepository<T,R> jpaRepository, List<R> ids, boolean isDeleteDb) throws EasyException{
        boolean isSuccess = true;
        try{
            if(CollUtil.isEmpty(ids)){
                return isSuccess;
            }
            // 真删除
            if(isDeleteDb){
                jpaRepository.deleteAllById(ids);
            }else{
                // 假删除
                List<T> allById = jpaRepository.findAllById(ids);
                for (T t : allById) {
                    t.setIsEnabled(-1);
                }
                List<T> ts = jpaRepository.saveAll(allById);
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
    public <T,R extends BaseDto> Page<R>  findByPage(JpaSpecificationExecutor<T> jpaResipory,R pageParamsDto, Specification<T> jpaSpecification,Class<R> dtoClass) throws EasyException {
        Integer pageNo = pageParamsDto.getPageNo();
        Integer pageSize = pageParamsDto.getPageSize();
        Pageable pageOf = PageableTools.basicPage(pageNo, pageSize, "createTime");
        Page<T> all = jpaResipory.findAll(jpaSpecification, pageOf);
        List<T> content = all.getContent();
        List<R> rs = BeanUtil.copyToList(content, dtoClass);
        return new PageImpl<>(rs, all.getPageable(), all.getTotalElements());
    }

    /**
     * 一个排序字段 倒序
     * @author bokun.li
     * @date 2023/6/2
     */
    public <T,R extends BaseDto> Page<R>  findByPage(JpaSpecificationExecutor<T> jpaResipory,R pageParamsDto, Specification<T> jpaSpecification,Class<R> dtoClass,String orderField) throws EasyException {
        Integer pageNo = pageParamsDto.getPageNo();
        Integer pageSize = pageParamsDto.getPageSize();
        Pageable pageOf = PageableTools.basicPage(pageNo, pageSize, orderField);
        Page<T> all = jpaResipory.findAll(jpaSpecification, pageOf);
        List<T> content = all.getContent();
        List<R> rs = BeanUtil.copyToList(content, dtoClass);
        return new PageImpl<>(rs, all.getPageable(), all.getTotalElements());
    }

    /**
     * 自定义排序字段
     * @author bokun.li
     * @date 2023/6/2
     */
    public <T,R extends BaseDto> Page<R>  findByPage(JpaSpecificationExecutor<T> jpaResipory, R pageParamsDto, Specification<T> jpaSpecification, Class<R> dtoClass, SortDto...sortDtos) throws EasyException {
        Integer pageNo = pageParamsDto.getPageNo();
        Integer pageSize = pageParamsDto.getPageSize();
        Pageable pageOf = PageableTools.basicPage(pageNo, pageSize, sortDtos);
        Page<T> all = jpaResipory.findAll(jpaSpecification, pageOf);
        List<T> content = all.getContent();
        List<R> rs = BeanUtil.copyToList(content, dtoClass);
        return new PageImpl<>(rs, all.getPageable(), all.getTotalElements());
    }

    /**
     * 根据ID获取 已经启用的应用
     * @author bokun.li
     * @date 2023/6/2
     */
    public <T extends BaseEntity,R extends BaseDto,N> T getEnableOrDisableById(JpaRepository<T,N> jpaRepository, N id) throws EasyException {
        Optional<T> byId = jpaRepository.findById(id);
        if (byId.isPresent()) {
            T t1 = byId.get();
            int isEnabled = t1.getIsEnabled();
            if(isEnabled == 1 || isEnabled == 0){
                return t1;
            }else{
                throw new EasyException("要查询的记录不存在,或者已经被删除");
            }
        }else {
            throw new EasyException("要查询的记录不存在");
        }

    }

    public <T extends BaseDto> String getDtoId(T dto){
        Field[] fields = ReflectUtil.getFields(dto.getClass(), e -> e.isAnnotationPresent(Id.class));
        if(fields.length == 0){
            throw new EasyException("dto没有Id注解");
        }else{
            Field field = fields[0];
            String fieldName = ReflectUtil.getFieldName(field);
            Object fieldValue = ReflectUtil.getFieldValue(dto, fieldName);
            if (fieldValue == null){
                throw new EasyException("主键为空");
            }
            return fieldValue.toString();
        }
    }

    public <T extends BaseDto> int getDtoVersion(T dto){
        Field[] fields = ReflectUtil.getFields(dto.getClass(), e -> e.isAnnotationPresent(Version.class));
        if(fields.length == 0){
            throw new EasyException("dto没有Version注解");
        }else{
            Field field = fields[0];
            String fieldName = ReflectUtil.getFieldName(field);
            Object fieldValue = ReflectUtil.getFieldValue(dto, fieldName);
            if(fieldValue == null){
                throw new EasyException("版本号version为空");
            }
            String string = fieldValue.toString();
            if (StrUtil.isNumeric(string)) {
                return Integer.parseInt(string);
            }else{
                throw new EasyException("版本号version非数字");
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
