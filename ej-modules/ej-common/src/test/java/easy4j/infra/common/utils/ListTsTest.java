package easy4j.infra.common.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static easy4j.infra.common.utils.ListTs.pickByClass;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
class ListTsTest {

    // 测试从混合类型列表中筛选特定类的元素
    @Test
    void shouldPickElementsOfTargetClass() {
        // 准备测试数据
        List<Object> originList = Arrays.asList(
                "string1", 123, "string2", 45.67, "string3", null
        );

        // 执行测试方法
        List<String> result = pickByClass(originList, String.class);

        assertThat(pickByClass(originList, Integer.class)).hasSize(1)
                .containsExactly(123);

        assertThat(pickByClass(originList, Double.class)).hasSize(1)
                .containsExactly(45.67);
        // 验证结果
        assertThat(result).hasSize(3)
                .containsExactly("string1", "string2", "string3");
    }

    // 测试空输入列表
    @Test
    void shouldReturnEmptyListWhenOriginListIsEmpty() {
        List<Object> originList = Collections.emptyList();

        List<Integer> result = pickByClass(originList, Integer.class);

        assertThat(result).isEmpty();
    }

    // 测试输入列表全为null的情况
    @Test
    void shouldReturnEmptyListWhenAllElementsAreNull() {
        List<Object> originList = Arrays.asList(null, null, null);

        List<Double> result = pickByClass(originList, Double.class);

        assertThat(result).isEmpty();
    }

    // 测试没有匹配类型的元素
    @Test
    void shouldReturnEmptyListWhenNoMatchingElements() {
        List<Object> originList = Arrays.asList(1, 2, 3, 4);

        List<String> result = pickByClass(originList, String.class);

        assertThat(result).isEmpty();
    }

    // 测试处理继承关系（子类实例可以被父类筛选出来）
    @Test
    void shouldPickSubclassInstancesWhenTargetIsSuperclass() {
        class Animal {}
        class Dog extends Animal {}
        class Cat extends Animal {}

        List<Object> originList = Arrays.asList(new Dog(), new Cat(), new Animal(), "not animal");

        List<Animal> result = pickByClass(originList, Animal.class);

        assertThat(result).hasSize(3);
        assertThat(result).filteredOn(instance -> instance instanceof Dog).hasSize(1);
        assertThat(result).filteredOn(instance -> instance instanceof Cat).hasSize(1);
        assertThat(result).filteredOn(instance -> instance.getClass() == Animal.class).hasSize(1);
    }

    // 测试基本类型包装类的筛选
    @Test
    void shouldHandleWrapperClasses() {
        List<Object> originList = Arrays.asList(100, 200L, 300.0, "400");

        List<Integer> result = pickByClass(originList, Integer.class);

        assertThat(result).containsExactly(100);
    }
}

