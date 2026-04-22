package io.github.lbkones.common;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 元组工具类，提供创建、操作和转换元组的功能
 * 元组是函数式编程中常用的数据结构，用于将多个值组合成一个单一对象
 * 适用于需要返回多个值的场景，替代使用数组或自定义类
 */
public final class TupleUtils {

    // 私有构造方法，防止实例化
    private TupleUtils() {
        throw new AssertionError("工具类不能实例化");
    }

    /**
     * =====================
     * 元组创建方法
     * =====================
     */

    /**
     * 创建一个二元组（Pair）
     * @param first 第一个元素
     * @param second 第二个元素
     * @param <T1> 第一个元素类型
     * @param <T2> 第二个元素类型
     * @return 新创建的二元组
     */
    public static <T1, T2> Pair<T1, T2> pair(T1 first, T2 second) {
        return new Pair<>(first, second);
    }

    /**
     * 创建一个三元组（Triple）
     * @param first 第一个元素
     * @param second 第二个元素
     * @param third 第三个元素
     * @param <T1> 第一个元素类型
     * @param <T2> 第二个元素类型
     * @param <T3> 第三个元素类型
     * @return 新创建的三元组
     */
    public static <T1, T2, T3> Triple<T1, T2, T3> triple(T1 first, T2 second, T3 third) {
        return new Triple<>(first, second, third);
    }

    /**
     * 创建一个四元组（Quadruple）
     * @param first 第一个元素
     * @param second 第二个元素
     * @param third 第三个元素
     * @param fourth 第四个元素
     * @param <T1> 第一个元素类型
     * @param <T2> 第二个元素类型
     * @param <T3> 第三个元素类型
     * @param <T4> 第四个元素类型
     * @return 新创建的四元组
     */
    public static <T1, T2, T3, T4> Quadruple<T1, T2, T3, T4> quadruple(
            T1 first, T2 second, T3 third, T4 fourth) {
        return new Quadruple<>(first, second, third, fourth);
    }

    /**
     * =====================
     * 元组转换方法
     * =====================
     */

    /**
     * 将元组转换为列表
     * @param tuple 元组对象
     * @return 包含元组所有元素的列表
     */
    public static List<?> toList(Tuple tuple) {
        if (tuple == null) {
            return null;
        }
        return tuple.toList();
    }

    /**
     * 将元组转换为数组
     * @param tuple 元组对象
     * @return 包含元组所有元素的数组
     */
    public static Object[] toArray(Tuple tuple) {
        if (tuple == null) {
            return null;
        }
        return tuple.toArray();
    }

    /**
     * =====================
     * 元组比较方法
     * =====================
     */

    /**
     * 比较两个元组是否相等
     * @param tuple1 第一个元组
     * @param tuple2 第二个元组
     * @return 相等返回true，否则返回false
     */
    public static boolean equals(Tuple tuple1, Tuple tuple2) {
        if (tuple1 == tuple2) {
            return true;
        }
        if (tuple1 == null || tuple2 == null) {
            return false;
        }
        return tuple1.equals(tuple2);
    }

    /**
     * 计算元组的哈希码
     * @param tuple 元组对象
     * @return 哈希码值
     */
    public static int hashCode(Tuple tuple) {
        return tuple != null ? tuple.hashCode() : 0;
    }

    /**
     * =====================
     * 内部元组接口和实现
     * =====================
     */

    /**
     * 元组基础接口，定义元组的基本行为
     */
    public interface Tuple {
        /**
         * 获取元组的大小（元素数量）
         * @return 元素数量
         */
        int size();

        /**
         * 将元组转换为列表
         * @return 包含所有元素的列表
         */
        List<?> toList();

        /**
         * 将元组转换为数组
         * @return 包含所有元素的数组
         */
        Object[] toArray();
    }

    /**
     * 二元组实现
     */
    public static class Pair<T1, T2> implements Tuple {
        private final T1 first;
        private final T2 second;

        public Pair(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }

        public T1 getFirst() {
            return first;
        }

        public T2 getSecond() {
            return second;
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public List<?> toList() {
            return Arrays.asList(first, second);
        }

        @Override
        public Object[] toArray() {
            return new Object[]{first, second};
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(first, pair.first) &&
                   Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }

        @Override
        public String toString() {
            return "Pair(" + first + ", " + second + ")";
        }
    }

    /**
     * 三元组实现
     */
    public static class Triple<T1, T2, T3> implements Tuple {
        private final T1 first;
        private final T2 second;
        private final T3 third;

        public Triple(T1 first, T2 second, T3 third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        public T1 getFirst() {
            return first;
        }

        public T2 getSecond() {
            return second;
        }

        public T3 getThird() {
            return third;
        }

        @Override
        public int size() {
            return 3;
        }

        @Override
        public List<?> toList() {
            return Arrays.asList(first, second, third);
        }

        @Override
        public Object[] toArray() {
            return new Object[]{first, second, third};
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
            return Objects.equals(first, triple.first) &&
                   Objects.equals(second, triple.second) &&
                   Objects.equals(third, triple.third);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second, third);
        }

        @Override
        public String toString() {
            return "Triple(" + first + ", " + second + ", " + third + ")";
        }
    }

    /**
     * 四元组实现
     */
    public static class Quadruple<T1, T2, T3, T4> implements Tuple {
        private final T1 first;
        private final T2 second;
        private final T3 third;
        private final T4 fourth;

        public Quadruple(T1 first, T2 second, T3 third, T4 fourth) {
            this.first = first;
            this.second = second;
            this.third = third;
            this.fourth = fourth;
        }

        public T1 getFirst() {
            return first;
        }

        public T2 getSecond() {
            return second;
        }

        public T3 getThird() {
            return third;
        }

        public T4 getFourth() {
            return fourth;
        }

        @Override
        public int size() {
            return 4;
        }

        @Override
        public List<?> toList() {
            return Arrays.asList(first, second, third, fourth);
        }

        @Override
        public Object[] toArray() {
            return new Object[]{first, second, third, fourth};
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Quadruple<?, ?, ?, ?> quadruple = (Quadruple<?, ?, ?, ?>) o;
            return Objects.equals(first, quadruple.first) &&
                   Objects.equals(second, quadruple.second) &&
                   Objects.equals(third, quadruple.third) &&
                   Objects.equals(fourth, quadruple.fourth);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second, third, fourth);
        }

        @Override
        public String toString() {
            return "Quadruple(" + first + ", " + second + ", " + third + ", " + fourth + ")";
        }
    }
}