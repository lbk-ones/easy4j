package io.github.lbkones.common;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * List工具类，提供丰富的集合操作方法
 * 简化Stream API使用，提供更简洁的替代方案
 * 包含基础操作、高级处理和集合运算等功能
 */
public final class ListUtils {

    // 私有构造方法，防止实例化
    private ListUtils() {
        throw new AssertionError("工具类不能实例化");
    }

    /**
     * =====================
     * 基础集合操作
     * =====================
     */

    /**
     * 判断列表是否为空（null或长度为0）
     * @param list 待检查的列表
     * @return 为空返回true，否则返回false
     */
    public static boolean isEmpty(Collection<?> list) {
        return list == null || list.isEmpty();
    }

    /**
     * 判断列表是否不为空（非null且长度大于0）
     * @param list 待检查的列表
     * @return 不为空返回true，否则返回false
     */
    public static boolean isNotEmpty(Collection<?> list) {
        return !isEmpty(list);
    }

    /**
     * 获取列表大小，安全处理null
     * @param list 待获取大小的列表
     * @return 列表大小，null返回0
     */
    public static int size(Collection<?> list) {
        return list == null ? 0 : list.size();
    }

    /**
     * 创建空列表
     * @param <T> 元素类型
     * @return 空列表
     */
    public static <T> List<T> newArrayList() {
        return new ArrayList<>();
    }

    /**
     * 创建包含指定元素的列表
     * @param elements 元素
     * @param <T> 元素类型
     * @return 包含指定元素的列表
     */
    @SafeVarargs
    public static <T> List<T> of(T... elements) {
        List<T> list = new ArrayList<>();
        Collections.addAll(list, elements);
        return list;
    }

    /**
     * 将集合转换为列表
     * @param collection 集合
     * @param <T> 元素类型
     * @return 转换后的列表
     */
    public static <T> List<T> toList(Collection<T> collection) {
        if (isEmpty(collection)) {
            return newArrayList();
        }
        return new ArrayList<>(collection);
    }

    /**
     * =====================
     * 简化Stream操作
     * =====================
     */

    /**
     * 过滤列表，保留满足条件的元素
     * @param list 源列表
     * @param predicate 过滤条件
     * @param <T> 元素类型
     * @return 过滤后的列表
     */
    public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        if (isEmpty(list)) {
            return newArrayList();
        }
        return list.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * 映射转换列表元素
     * @param source 源列表
     * @param mapper 转换函数
     * @param <T> 源元素类型
     * @param <R> 目标元素类型
     * @return 转换后的列表
     */
    public static <T, R> List<R> map(List<T> source, Function<T, R> mapper) {
        if (isEmpty(source)) {
            return newArrayList();
        }
        return source.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * 扁平化映射转换列表元素
     * @param source 源列表
     * @param mapper 转换函数
     * @param <T> 源元素类型
     * @param <R> 目标元素类型
     * @return 转换后的列表
     */
    public static <T, R> List<R> flatMap(List<T> source, Function<T, List<R>> mapper) {
        if (isEmpty(source)) {
            return newArrayList();
        }
        return source.stream()
                .flatMap(t -> mapper.apply(t).stream())
                .collect(Collectors.toList());
    }

    /**
     * 排序列表
     * @param list 待排序列表
     * @param comparator 比较器
     * @param <T> 元素类型
     * @return 排序后的列表
     */
    public static <T> List<T> sorted(List<T> list, Comparator<T> comparator) {
        if (isEmpty(list)) {
            return newArrayList();
        }
        return list.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * 求和（适用于数字列表）
     * @param list 数字列表
     * @return 求和结果
     */
    public static double sum(List<? extends Number> list) {
        if (isEmpty(list)) {
            return 0;
        }
        return list.stream()
                .mapToDouble(Number::doubleValue)
                .sum();
    }

    /**
     * 求平均值（适用于数字列表）
     * @param list 数字列表
     * @return 平均值
     */
    public static double average(List<? extends Number> list) {
        if (isEmpty(list)) {
            return 0;
        }
        return list.stream()
                .mapToDouble(Number::doubleValue)
                .average()
                .orElse(0);
    }

    /**
     * =====================
     * 高级集合功能
     * =====================
     */

    /**
     * 分组列表元素
     * @param list 源列表
     * @param classifier 分组函数
     * @param <T> 元素类型
     * @param <K> 分组键类型
     * @return 分组后的映射
     */
    public static <T, K> Map<K, List<T>> groupBy(List<T> list, Function<T, K> classifier) {
        if (isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream()
                .collect(Collectors.groupingBy(classifier));
    }

    /**
     * 分页获取列表
     * @param list 源列表
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @param <T> 元素类型
     * @return 分页后的列表
     */
    public static <T> List<T> paginate(List<T> list, int page, int size) {
        if (isEmpty(list) || page < 1 || size < 1) {
            return newArrayList();
        }
        int start = (page - 1) * size;
        if (start >= list.size()) {
            return newArrayList();
        }
        int end = Math.min(start + size, list.size());
        return list.subList(start, end);
    }

    /**
     * 去除列表中的重复元素
     * @param list 源列表
     * @param <T> 元素类型
     * @return 去重后的列表
     */
    public static <T> List<T> distinct(List<T> list) {
        if (isEmpty(list)) {
            return newArrayList();
        }
        return list.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 合并多个列表
     * @param lists 要合并的列表
     * @param <T> 元素类型
     * @return 合并后的列表
     */
    @SafeVarargs
    public static <T> List<T> merge(List<T>... lists) {
        List<T> result = new ArrayList<>();
        for (List<T> list : lists) {
            if (isNotEmpty(list)) {
                result.addAll(list);
            }
        }
        return result;
    }

    /**
     * 获取两个列表的交集
     * @param list1 第一个列表
     * @param list2 第二个列表
     * @param <T> 元素类型
     * @return 交集列表
     */
    public static <T> List<T> intersection(List<T> list1, List<T> list2) {
        if (isEmpty(list1) || isEmpty(list2)) {
            return newArrayList();
        }
        return list1.stream()
                .filter(list2::contains)
                .collect(Collectors.toList());
    }

    /**
     * 获取两个列表的并集
     * @param list1 第一个列表
     * @param list2 第二个列表
     * @param <T> 元素类型
     * @return 并集列表
     */
    public static <T> List<T> union(List<T> list1, List<T> list2) {
        if (isEmpty(list1)) {
            return toList(list2);
        }
        if (isEmpty(list2)) {
            return toList(list1);
        }
        List<T> result = new ArrayList<>(list1);
        for (T item : list2) {
            if (!result.contains(item)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 获取两个列表的差集（list1 - list2）
     * @param list1 第一个列表
     * @param list2 第二个列表
     * @param <T> 元素类型
     * @return 差集列表
     */
    public static <T> List<T> difference(List<T> list1, List<T> list2) {
        if (isEmpty(list1)) {
            return newArrayList();
        }
        if (isEmpty(list2)) {
            return toList(list1);
        }
        return list1.stream()
                .filter(item -> !list2.contains(item))
                .collect(Collectors.toList());
    }

    /**
     * =====================
     * 其他实用方法
     * =====================
     */

    /**
     * 检查列表是否包含指定元素
     * @param list 列表
     * @param element 元素
     * @param <T> 元素类型
     * @return 包含返回true，否则返回false
     */
    public static <T> boolean contains(List<T> list, T element) {
        return isNotEmpty(list) && list.contains(element);
    }

    /**
     * 查找列表中第一个满足条件的元素
     * @param list 列表
     * @param predicate 条件
     * @param <T> 元素类型
     * @return 找到的元素，未找到返回null
     */
    public static <T> T findFirst(List<T> list, Predicate<T> predicate) {
        if (isEmpty(list)) {
            return null;
        }
        return list.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查所有元素是否满足条件
     * @param list 列表
     * @param predicate 条件
     * @param <T> 元素类型
     * @return 所有元素满足条件返回true，否则返回false
     */
    public static <T> boolean allMatch(List<T> list, Predicate<T> predicate) {
        if (isEmpty(list)) {
            return false;
        }
        return list.stream().allMatch(predicate);
    }

    /**
     * 检查是否存在元素满足条件
     * @param list 列表
     * @param predicate 条件
     * @param <T> 元素类型
     * @return 存在满足条件的元素返回true，否则返回false
     */
    public static <T> boolean anyMatch(List<T> list, Predicate<T> predicate) {
        if (isEmpty(list)) {
            return false;
        }
        return list.stream().anyMatch(predicate);
    }

    /**
     * 将列表转换为字符串，使用指定分隔符
     * @param list 列表
     * @param delimiter 分隔符
     * @param <T> 元素类型
     * @return 转换后的字符串
     */
    public static <T> String join(List<T> list, String delimiter) {
        if (isEmpty(list)) {
            return "";
        }
        if (delimiter == null) {
            delimiter = "";
        }
        return list.stream()
                .map(Object::toString)
                .collect(Collectors.joining(delimiter));
    }
}