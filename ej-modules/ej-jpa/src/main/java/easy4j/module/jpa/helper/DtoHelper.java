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
package easy4j.module.jpa.helper;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.exception.EasyException;
import easy4j.module.jpa.annotations.CopyToNewEntity;
import easy4j.module.jpa.annotations.CopyToOldEntity;
import easy4j.module.jpa.base.BaseDto;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

/**
 * 将Dto转换为实体
 * 根据注释的属性关系进行转换
 * 注释CopyToNewEntity表示生成新的实体
 * 注释CopyToOldEntity表示修改实体
 */
@Slf4j
public class DtoHelper {

    /**
     * 生成新的实体，但字段对应关系不包含supper类的字段
     *
     * @param dto
     * @param clazz
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws EasyException
     * @throws InstantiationException
     */
    public static <T> T copyDtoToNewEntity(Object dto, Class clazz) throws IllegalAccessException, EasyException, InstantiationException {
        if (!(dto instanceof BaseDto)) {
            throw new EasyException("The object type is not a BaseDto");
        }
        Object entity = clazz.newInstance();
        Class<? extends Object> dtoClazz = dto.getClass();
        for (Field field : dtoClazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(CopyToNewEntity.class)) {
                Object o = field.get(dto);
                if (o == null) {
                    continue;
                }
                CopyToNewEntity annotation = field.getAnnotation(CopyToNewEntity.class);
                String fieldName = annotation.filed();
                if (StrUtil.isEmpty(fieldName)) continue;
                boolean isInParent = true;
                for (Field entityField : clazz.getDeclaredFields()) {
                    entityField.setAccessible(true);
                    if (fieldName.equals(entityField.getName())) {
                        //Object entityFieldObject = entityField.get(entity);
                        entityField.set(entity, o);
                        isInParent = false;
                        break;
                    }
                }
                if (isInParent) {
                    setSuperField(entity, fieldName, o);
                }
            }
        }
        return (T) entity;
    }

    /**
     * 修改原来的实体，但字段对应关系中不包括修改supper类
     *
     * @param dto
     * @param old
     * @param <T>
     * @return
     * @throws EasyException
     * @throws IllegalAccessException
     */
    public static <T> T copyDtoToOldEntity(Object dto, T old) throws EasyException, IllegalAccessException {
        if (!(dto instanceof BaseDto)) {
            throw new EasyException("The object type is not a BaseDto");
        }
        Class<? extends Object> clazz = old.getClass();
        Class<? extends Object> dtoClazz = dto.getClass();
        for (Field field : dtoClazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(CopyToOldEntity.class)) {
                Object o = field.get(dto);
                if (o == null) {
                    continue;
                }
                CopyToOldEntity annotation = field.getAnnotation(CopyToOldEntity.class);
                String fieldName = annotation.filed();
                if (StrUtil.isEmpty(fieldName)) continue;
                boolean isInParent = true;
                for (Field entityField : clazz.getDeclaredFields()) {
                    entityField.setAccessible(true);
                    if (fieldName.equals(entityField.getName())) {
                        //Object entityFieldObject = entityField.get(entity);
                        entityField.set(old, o);
                        isInParent = false;
                        break;
                    }
                }
                if (isInParent) {
                    setSuperField(old, fieldName, o);
                }
            }
        }
        return old;
    }

    private static void setSuperField(Object childrenClass, String filedName,
                                      Object filedValue) {
        Field field = null;
        try {
            Class parent = childrenClass.getClass().getSuperclass();
            if (parent == null || parent.getName().toLowerCase().equals("java.lang.object")) {
                return;
            }
            field = parent.getDeclaredField(filedName);
            field.setAccessible(true);
            field.set(childrenClass, filedValue);
        } catch (Exception e) {
            log.warn("set field value to parent failed: " + e.getMessage());
        }
    }
}
