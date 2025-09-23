package easy4j.infra.quartz;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

/**
 * 扫描带@Easy4jQzJob注解的类
 */
public class QuartzJobScanner {

    /**
     * 扫描指定包下所有带@Easy4jQzJob注解的类
     */
    public static Set<BeanDefinition> scan(String basePackage) {
        // 创建类路径扫描器
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        // 添加注解过滤器（只扫描带@Easy4jQzJob的类）
        scanner.addIncludeFilter(new AnnotationTypeFilter(Easy4jQzJob.class));

        // 扫描指定包
        return scanner.findCandidateComponents(basePackage);
    }
}
