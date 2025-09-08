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
package easy4j.infra.common.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.annotations.Desc;
import lombok.var;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 不借助其他工具类 简单封装一些常用集合方法
 * 空集合创建
 * 从集合里面拿某个字段 单独组成集合
 * 集合根据某个字段分组
 * 集合根据某个字段映射 类似于 js中的 Arrays.map()
 * 集合长度拆分
 * 简单遍历
 *
 * @author bokun.li
 * @date 2023/6/1
 */
public class ListTs {


    // 注意线程安全
    public static <T> Stream<T> asStream(List<T> list) {
        Stream<T> empty = Stream.empty();
        if (CollUtil.isNotEmpty(list)) {
            if (list.size() >= 3000) {
                return list.parallelStream();
            } else {
                return list.stream();
            }
        }
        return empty;
    }


    /**
     * 从集合里面 收集string类型的参数集合 排除掉null值
     *
     * @author bokun.li
     * @date 2023/6/1
     */
    public static <T> List<String> mapStringToList(List<T> w, Function<T, String> function) {
        List<String> result = newArrayList();

        if (!isEmpty(w) && null != function) {
            List<String> collect = asStream(w).map(function).filter(StrUtil::isNotBlank).collect(Collectors.toList());
            if (isNotEmpty(collect)) {
                result.addAll(collect);
            }
        }
        return result;
    }

    /**
     * 动态返回集合的类型
     *
     * @param w
     * @param function
     * @param <R>
     * @param <T>
     * @return
     */
    @Desc("动态返回集合的类型")
    public static <R, T> List<R> mapToList(List<T> w, Function<T, R> function) {
        List<R> result = newArrayList();
        if (!isEmpty(w) && null != function) {
            List<R> collect = asStream(w).map(function).filter(ObjectUtil::isNotEmpty).collect(Collectors.toList());
            if (isNotEmpty(collect)) {
                result.addAll(collect);
            }
        }
        return result;
    }

    @Desc("动态返回集合的类型,去从")
    public static <R, T> List<R> mapDistinctToList(List<T> w, Function<T, R> function) {
        List<R> result = newArrayList();
        if (!isEmpty(w) && null != function) {
            List<R> collect = asStream(w).map(function).filter(ObjectUtil::isNotEmpty).distinct().collect(Collectors.toList());
            if (isNotEmpty(collect)) {
                return collect;
            }
        }
        return result;
    }

    public static <T> List<String> mapStrDistToList(List<T> w, Function<T, String> function) {
        List<String> list = mapStringToList(w, function);
        return distinct(list, Function.identity());
    }

    /**
     * 根据某个String类型的字段分组 因为分组的时候如果 要分组的那个字段为空那么 就会报错  顺带兼容一下
     *
     * @author bokun.li
     * @date 2023/6/1
     */
    public static <T> Map<String, List<T>> groupByStrKey(List<T> w, Function<T, String> function) {
        Map<String, List<T>> result = new HashMap<>();
        if (isNotEmpty(w) && null != function) {
            Map<String, List<T>> collect1 = asStream(w).collect(Collectors.groupingBy(e1 -> {
                String apply = function.apply(e1);
                if (Objects.nonNull(apply)) {
                    return apply;
                }
                return "-";
            }));
            result.putAll(collect1);
        }
        return result;
    }

    @Desc("动态返回key的类型，过滤掉返回值为null的元素，groupBy null值会报错")
    public static <R, T> Map<R, List<T>> groupBy(List<T> w, Function<T, R> function) {
        Map<R, List<T>> result = new HashMap<>();
        if (isNotEmpty(w) && null != function) {
            return asStream(w).filter(e -> function.apply(e) != null).collect(Collectors.groupingBy(function));
        }
        return result;
    }

    /**
     * 将集合中的某个元素和对象对应起来 相同key取第一个
     *
     * @author bokun.li
     * @date 2023/6/1
     */
    public static <T> Map<String, T> mapOne(List<T> w, Function<T, String> function) {
        Map<String, T> result = new HashMap<>();
        if (isNotEmpty(w) && null != function) {
            Map<String, T> collect = asStream(w).filter(e -> StrUtil.isNotBlank(function.apply(e))).collect(Collectors.toMap(function, Function.identity(), (k1, k2) -> k1));
            if (isNotEmpty(collect)) {
                result.putAll(collect);
            }
        }
        return result;
    }

    /**
     * 转为map 根据泛型来决定key的类型
     *
     * @param w
     * @param function
     * @param <R>
     * @param <T>
     * @return
     */
    @Desc("转为map 根据泛型来决定key的类型")
    public static <R, T> Map<R, T> toMap(List<T> w, Function<T, R> function) {
        Map<R, T> result = new HashMap<>();
        if (isNotEmpty(w) && null != function) {
            Map<R, T> collect = asStream(w).filter(e -> function.apply(e) != null).collect(Collectors.toMap(function, Function.identity(), (k1, k2) -> k1));
            if (isNotEmpty(collect)) {
                return collect;
            }
        }
        return result;
    }

    @Desc("将集合转为另外一个类型的集合，同时去除null值")
    public static <R, T> List<R> map(List<T> w, Function<T, R> function) {
        List<R> list = newList();
        if (isNotEmpty(w) && null != function) {
            List<R> collect = asStream(w).filter(Objects::nonNull).map(function).filter(Objects::nonNull).collect(Collectors.toList());
            if (isNotEmpty(collect)) {
                return collect;
            }
        }
        return list;
    }

    /**
     * 集合根据len拆分 集合
     *
     * @author bokun.li
     * @date 2023/6/1
     */
    public static <T> List<List<T>> partition(List<T> listT, int len) {
        List<List<T>> result = newArrayList();
        if (isNotEmpty(listT)) {
            int allSize = listT.size();
            if (len <= 0 || len > allSize) {
                result.add(listT);
                return result;
            }
            int yz = allSize / len;
            int qy = allSize % len;
            for (int j = 0; j < yz; j++) {
                int i = j * len;
                List<T> ts = listT.subList(i, i + len);
                result.add(ts);
            }
            if (qy > 0) {
                List<T> ts = listT.subList(allSize - qy, allSize);
                result.add(ts);
            }
        }
        return result;
    }

    /**
     * 简单过滤
     *
     * @author bokun.li
     * @date 2023/6/1
     */
    public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        List<T> result = newArrayList();
        if (isNotEmpty(list)) {
            List<T> collect = asStream(list).filter(predicate).collect(Collectors.toList());
            if (isNotEmpty(collect)) {
                result.addAll(collect);
            }
        }
        return result;
    }

    /**
     * 不返回新数组
     *
     * @author bokun.li
     * @date 2023/8/17
     */
    public static <T> List<T> filter2(List<T> list, Predicate<T> predicate) {
        return asStream(list).filter(predicate).collect(Collectors.toList());
    }

    public static <T> ArrayList<T> newArrayList() {
        return new ArrayList<>();
    }

    public static <T> List<T> newList() {
        return new ArrayList<>();
    }

    public static <T> List<T> newLinkedList() {
        return new LinkedList<>();
    }

    public static <T> List<T> newCopyOnWriteArrayList() {
        return new CopyOnWriteArrayList<>();
    }

    public static <T> List<T> newLinkedList(Collection<T> collection) {
        return new LinkedList<>(collection);
    }

    public static <T> List<T> newArrayList(Iterator<T> iterator) {
        List<T> objects = newArrayList();
        if (Objects.nonNull(iterator)) {
            while (iterator.hasNext()) {
                T next = iterator.next();
                objects.add(next);
            }
        }
        return objects;
    }

    public static <T> boolean isEmpty(Collection<T> collection) {
        return null == collection || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return null == map || map.isEmpty();
    }

    public static boolean isEmpty(Object[] map) {
        return null == map || map.length == 0;
    }

    // 判断int数组是否为空
    public static boolean isEmpty(int[] array) {
        return array == null || array.length == 0;
    }

    // 判断char数组是否为空
    public static boolean isEmpty(char[] array) {
        return array == null || array.length == 0;
    }

    // 判断double数组是否为空
    public static boolean isEmpty(double[] array) {
        return array == null || array.length == 0;
    }

    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }

    public static <T> boolean isNotEmpty(T[] collection) {
        return collection != null && collection.length > 0;
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    @SafeVarargs
    public static <T> List<T> asList(T... id) {
        List<T> objects = newArrayList();
        objects.addAll(Arrays.asList(id));
        return objects;
    }

    public static List<String> randomStrList(int length) {
        List<String> objects = newArrayList();
        for (int i = 0; i < length; i++) {
            char c = RandomUtil.randomChar();
            objects.add(String.valueOf(c));
        }
        return objects;
    }

    /**
     * 根据条件去重
     * 对象的话后面的会 覆盖前面的
     *
     * @author bokun.li
     * @date 2023/6/14
     */
    public static <T, R> List<T> distinct(List<T> allList, Function<T, R> function) {
        List<T> objects = newArrayList();
        if (isEmpty(allList)) {
            return objects;
        }
        Map<R, T> hashMap = new HashMap<>();
        boolean isSingle = false;
        boolean we = false;
        for (int i = 0; i < allList.size(); i++) {
            T t = allList.get(i);
            if (!we && isBasic(t)) {
                isSingle = true;
                break;
            } else {
                we = true;
                if (null != function) {
                    R apply = function.apply(t);
                    if (Objects.nonNull(apply)) {
                        hashMap.put(apply, t);
                    }
                }
            }
        }
        if (isSingle && CollUtil.isNotEmpty(allList)) {
            List<T> collect = asStream(allList).distinct().collect(Collectors.toList());
            objects.addAll(collect);
        } else if (!isSingle) {
            Collection<T> values = hashMap.values();
            objects.addAll(values);
        }
        return objects;
    }

    public static boolean isBasic(Object param) {
        boolean isBasic = false;
        if (param instanceof Integer) {
            isBasic = true;
        } else if (param instanceof String) {
            isBasic = true;
        } else if (param instanceof Double) {
            isBasic = true;
        } else if (param instanceof Float) {
            isBasic = true;
        } else if (param instanceof Long) {
            isBasic = true;
        } else if (param instanceof Boolean) {
            isBasic = true;
        }
        return isBasic;
    }

    /**
     * 循环拷贝集合 实测 相比于 hutool提供的那个性能要好 好几倍吧应该
     *
     * @author bokun.li
     * @date 2023/8/6
     */
    public static <T, R> List<R> copyList(List<T> source, Class<R> rClass) {
        List<R> objects = ListTs.newArrayList();
        if (CollUtil.isEmpty(source)) {
            return objects;
        }
        for (T t : source) {
            R r = ReflectUtil.newInstanceIfPossible(rClass);
            BeanUtil.copyProperties(t, r);
            objects.add(r);
        }
        return objects;
    }

    public static <T> T getOrDefault(List<T> reqs, int i, Class<T> clazz) {
        try {
            if (CollUtil.isNotEmpty(reqs)) {
                return reqs.get(i);
            }
        } catch (Exception ignored) {

        }
        if (Objects.isNull(clazz)) {
            return null;
        }
        return ReflectUtil.newInstance(clazz);
    }

    public static <T> T get(List<T> reqs, int i) {
        try {
            if (CollUtil.isNotEmpty(reqs)) {
                return reqs.get(i);
            }
        } catch (Exception ignored) {

        }
        return null;
    }

    public static <T> T get(T[] reqs, int i) {
        try {
            if (reqs != null) {
                return reqs[i];
            }
        } catch (Exception ignored) {

        }
        return null;
    }

    public static <T> T get(List<T> reqs, int i, T defaultValue) {
        try {
            if (CollUtil.isNotEmpty(reqs)) {
                return reqs.get(i);
            }
        } catch (Exception ignored) {

        }
        return defaultValue;
    }

    public static <T> void foreach(List<T> list, Consumer<T> consumer) {
        if (CollUtil.isNotEmpty(list) && Objects.nonNull(consumer)) {
            list.forEach(consumer);
        }
    }

    @SafeVarargs
    public static <T> List<T> concat(T[]... value) {
        Set<T> objects = new HashSet<>();
        for (T[] ts : value) {
            objects.addAll(Arrays.asList(ts));
        }
        return newArrayList(objects.iterator());
    }

    /**
     * 遍历传入的Object 如果传入的Object是个数组或者是个集合那么 就将数组或者集合的元素以Object形式来遍历
     *
     * @param obj
     * @param object
     */
    public static void loop(Object obj, Consumer<Object> object) {
        if (obj == null) {
            return;
        }
        if (obj.getClass().isArray()) {
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                object.accept(Array.get(obj, i));
            }
        } else if (obj instanceof Iterable) {
            for (Object element : (Iterable<?>) obj) {
                object.accept(element);
            }
        } else if (obj instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                object.accept(entry.getValue());
            }
        } else {
            object.accept(obj);
        }
    }

    /**
     * 将传入obj转为集合 集合的类型为Object
     *
     * @param obj_
     * @param convertFunction 可以为null 如果是null那么就不转换
     * @return
     */
    public static List<Object> objectToListObject(Object obj_, Function<Object, Object> convertFunction) {
        List<Object> mapResultList = ListTs.newArrayList();
        loop(obj_, obj -> {
            if (null != convertFunction) {
                Object apply = convertFunction.apply(obj);
                mapResultList.add(apply);
            } else {
                mapResultList.add(obj);
            }
        });
        return mapResultList;
    }

    /**
     * 数组展开
     *
     * @param array
     * @param flattened
     */
    public static void flatten(Object[] array, List<Object> flattened) {
        for (Object element : array) {
            if (element instanceof Object[]) {
                // 递归处理嵌套数组
                flatten((Object[]) element, flattened);
            } else if (element instanceof Iterable<?>) {
                Iterable<?> element1 = (Iterable<?>) element;
                element1.forEach(flattened::add);
            } else {
                flattened.add(element);
            }
        }
    }

    /**
     * 将传入的对象 遍历 将转换结果i 转为想要的类型 T
     * 如果传入的对象不是集合那么也转换为集合
     *
     * @param obj_            传入要转换的对象
     * @param aclass          要转换的类型class
     * @param convertFunction 转换函数
     * @param <T>
     * @return
     */
    public static <T> List<T> objectToListT(Object obj_, Class<T> aclass, Function<Object, Object> convertFunction) {
        List<T> mapResultList = ListTs.newArrayList();
        loop(obj_, obj -> {
            if (null != convertFunction) {
                Object apply = convertFunction.apply(obj);
                T convert = Convert.convert(aclass, apply);
                mapResultList.add(convert);
            } else {
                mapResultList.add(Convert.convert(aclass, obj));
            }
        });
        return mapResultList;
    }

    /**
     * 将传入的集合对象中的指定属性转为List<Object>
     * 如果传入的对象是个null 那么也返回一个空集合
     * 如果传入的对象是个空集合那么原路返回
     * 如果转换函数为空那么也原路返回
     *
     * @param obj_
     * @param convertFunction
     * @param <T>
     * @return
     */
    public static <T> List<Object> objListToListObjectByT(List<T> obj_, Function<T, Object> convertFunction) {
        if (obj_ == null) {
            return newArrayList();
        } else if (obj_.isEmpty()) {
            return objectToListObject(obj_, null);
        }
        if (Objects.isNull(convertFunction)) {
            return objectToListObject(obj_, null);
        }
        List<Object> mapResultList = ListTs.newArrayList();
        if (CollUtil.isEmpty(obj_)) {
            return mapResultList;
        }
        return obj_.stream().map(convertFunction).collect(Collectors.toList());
    }

    public static <T> List<String> tListToListString(List<T> obj_, Function<T, String> convertFunction) {
        List<String> mapResultList = ListTs.newArrayList();
        if (CollUtil.isEmpty(obj_)) {
            return mapResultList;
        }
        return obj_.stream().map(convertFunction).collect(Collectors.toList());
    }

    public static <T> List<T> singletonList(T object) {
        List<T> arrayList = new ArrayList<>();
        arrayList.add(object);
        return arrayList;
    }

    public static String join(String s, List<?> map) {
        if (isEmpty(map)) {
            return "";
        }
        return map.stream().map(String::valueOf).collect(Collectors.joining(s));
    }

    @Desc("根据指定的类型过滤出集合对象中的类型，如果是基本类型使用包装类型来匹配比如:Integer.class")
    public static <T> List<T> pickByClass(List<Object> originList, Class<T> tClass) {
        List<T> objects = ListTs.newList();
        if (tClass == null) {
            return objects;
        }
        if (CollUtil.isNotEmpty(originList)) {
            objects = originList.stream().map(e -> {
                if (ObjectUtil.isNotEmpty(e)) {
                    Class<?> aClass = e.getClass();
                    if (tClass.isAssignableFrom(aClass)) {
                        return Convert.convert(tClass, e);
                    }
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return objects;
    }

    @Desc("字符串拆分,根据sep来拆分")
    public static String[] split(String str, String sep) {
        if (StrUtil.isEmpty(str)) return new String[]{};

        if (StrUtil.isEmpty(sep)) {
            return new String[]{str};
        }

        String s = RegexEscapeUtils.escapeRegex(sep);
        return str.split(s);
    }

    public static List<String> splitToList(String str, String sep) {
        if (StrUtil.isEmpty(str)) return newList();

        if (StrUtil.isEmpty(sep)) {
            return asList(str);
        }

        String s = RegexEscapeUtils.escapeRegex(sep);
        String[] split = str.split(s);
        return ListTs.asList(split);
    }

    public static <T> void add(List<T> res, T obj) {

        if(ObjectUtil.isNotEmpty(obj)){
            res.add(obj);
        }

    }

    public static <T> void addAll(List<T> res, Collection<T> objs) {
        if(ObjectUtil.isNotEmpty(objs)){
            List<T> collect = objs.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if(isNotEmpty(collect)) res.addAll(collect);
        }
    }
}
