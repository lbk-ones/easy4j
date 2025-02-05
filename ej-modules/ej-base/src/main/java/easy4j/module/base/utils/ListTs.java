package easy4j.module.base.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;

import java.util.*;
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
 * @author bokun.li
 * @date 2023/6/1
 */
public class ListTs {


    // 注意线程安全
    public static <T> Stream<T> asStream(List<T> list){
        Stream<T> empty = Stream.empty();
        if(CollUtil.isNotEmpty(list)){
            if(list.size()>=3000){
                return list.parallelStream();
            }else{
                return list.stream();
            }
        }
        return empty;
    }


    /**
     * 从集合里面 收集string类型的参数集合 排除掉null值
     * @author bokun.li
     * @date 2023/6/1
     */
    public static <T> List<String> mapStringToList(List<T> w, Function<T,String> function){
        List<String> result = newArrayList();

        if (!isEmpty(w) && null != function) {
            List<String> collect = asStream(w).map(function).filter(StrUtil::isNotBlank).collect(Collectors.toList());
            if(isNotEmpty(collect)){
                result.addAll(collect);
            }
        }
        return result;
    }

    public static <T> List<String> mapStrDistToList(List<T> w, Function<T,String> function){
        List<String> list = mapStringToList(w, function);
        return distinct(list, Function.identity());
    }

    /**
     * 根据某个String类型的字段分组 因为分组的时候如果 要分组的那个字段为空那么 就会报错  顺带兼容一下
     * @author bokun.li
     * @date 2023/6/1
     */
    public static <T> Map<String, List<T>> groupByStrKey(List<T> w, Function<T,String> function){
        Map<String, List<T>> result = new HashMap<>();
        if (isNotEmpty(w) && null!=function) {
            Map<String, List<T>> collect1 = asStream(w).collect(Collectors.groupingBy(e1->{
                String apply = function.apply(e1);
                if(Objects.nonNull(apply)){
                    return apply;
                }
                return "-";
            }));
            result.putAll(collect1);
        }
        return result;
    }

    /**
     * 将集合中的某个元素和对象对应起来 相同key取第一个
     * @author bokun.li
     * @date 2023/6/1
     */
    public static <T> Map<String, T> mapOne(List<T> w, Function<T,String> function){
        Map<String, T> result = new HashMap<>();
        if (isNotEmpty(w) && null!=function) {
            Map<String, T> collect = asStream(w).collect(Collectors.toMap(function, Function.identity(), (k1, k2) -> k1));
            if(isNotEmpty(collect)){
                result.putAll(collect);
            }
        }
        return result;
    }

    /**
     * 集合根据len拆分 集合
     * @author bokun.li
     * @date 2023/6/1
     */
    public static <T> List<List<T>> partition(List<T> listT, int len){
        List<List<T>> result = newArrayList();
        if(isNotEmpty(listT)){
            int allSize = listT.size();
            if(len<=0 || len > allSize){
                result.add(listT);
                return result;
            }
            int yz = allSize / len;
            int qy = allSize % len;
            for (int j = 0; j <yz; j++) {
                int i = j * len;
                List<T> ts = listT.subList(i, i+len);
                result.add(ts);
            }
            if(qy>0){
                List<T> ts = listT.subList(allSize - qy, allSize);
                result.add(ts);
            }
        }
        return result;
    }
    /**
     * 简单过滤
     * @author bokun.li
     * @date 2023/6/1
     */
    public static <T> List<T> filter(List<T> list, Predicate<T> predicate){
        List<T> result = newArrayList();
        if(isNotEmpty(list)){
            List<T> collect = asStream(list).filter(predicate).collect(Collectors.toList());
            if(isNotEmpty(collect)){
                result.addAll(collect);
            }
        }
        return result;
    }

    /**
     * 不返回新数组
     * @author bokun.li
     * @date 2023/8/17
     */
    public static <T> List<T> filter2(List<T> list, Predicate<T> predicate){
        return asStream(list).filter(predicate).collect(Collectors.toList());
    }

    public  static <T> ArrayList<T> newArrayList(){
        return new ArrayList<>();
    }

    public  static <T> List<T> newLinkedList(){
        return new LinkedList<>();
    }

    public  static <T> List<T> newLinkedList(Collection<T> collection){
        return new LinkedList<>(collection);
    }

    public  static <T> List<T> newArrayList(Iterator<T> iterator){
        List<T> objects = newArrayList();
        if(Objects.nonNull(iterator)){
            while (iterator.hasNext()) {
                T next = iterator.next();
                objects.add(next);
            }
        }
        return objects;
    }

    public static <T> boolean isEmpty(Collection<T> collection){
        return null == collection || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map){
        return null == map || map.isEmpty();
    }

    public static <T> boolean isNotEmpty(Collection<T> collection){
        return !isEmpty(collection);
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static <T> List<T> asList(T ...id) {
        List<T> objects = newArrayList();
        objects.addAll(Arrays.asList(id));
        return objects;
    }

    public static  List<String> randomStrList(int length) {
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
     * @author bokun.li
     * @date 2023/6/14
     */
    public static <T,R> List<T> distinct(List<T> allList,Function<T,R> function) {
        List<T> objects = newArrayList();
        if(isEmpty(allList)){
            return objects;
        }
        Map<R, T> hashMap = new HashMap<>();
        boolean isSingle = false;
        boolean we = false;
        for (int i = 0; i < allList.size(); i++) {
            T t = allList.get(i);
            if(!we && isBasic(t)){
                isSingle = true;
                break;
            }else{
                we = true;
                if(null!=function){
                    R apply = function.apply(t);
                    if(Objects.nonNull(apply)){
                        hashMap.put(apply,t);
                    }
                }
            }
        }
        if(isSingle && CollUtil.isNotEmpty(allList)){
            List<T> collect = asStream(allList).distinct().collect(Collectors.toList());
            objects.addAll(collect);
        }else if(!isSingle){
            Collection<T> values = hashMap.values();
            objects.addAll(values);
        }
        return objects;
    }
    public static boolean isBasic(Object param){
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
     * @author bokun.li
     * @date 2023/8/6
     */
    public static <T,R> List<R> copyList(List<T> source,Class<R> rClass){
        List<R> objects = ListTs.newArrayList();
        if(CollUtil.isEmpty(source)){
            return objects;
        }
        for (T t : source) {
            R r = ReflectUtil.newInstanceIfPossible(rClass);
            BeanUtil.copyProperties(t,r);
            objects.add(r);
        }
        return objects;
    }

    public static <T> T getOrDefault(List<T> reqs, int i,Class<T> clazz) {
        try{
            if(CollUtil.isNotEmpty(reqs)){
                return reqs.get(i);
            }
        }catch (Exception e){

        }
        if(Objects.isNull(clazz)){
            return null;
        }
        return ReflectUtil.newInstance(clazz);
    }

    public static <T> T get(List<T> reqs, int i) {
        try{
            if(CollUtil.isNotEmpty(reqs)){
                return reqs.get(i);
            }
        }catch (Exception e){

        }
        return null;
    }

    public static <T> void foreach(List<T> list, Consumer<T> consumer) {
        if(CollUtil.isNotEmpty(list) && Objects.nonNull(consumer)){
            list.forEach(consumer);
        }
    }
}
