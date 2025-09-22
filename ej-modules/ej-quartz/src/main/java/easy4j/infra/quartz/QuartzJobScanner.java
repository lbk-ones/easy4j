package easy4j.infra.quartz;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

/**
 * 扫描带@QuartzJob注解的类
 */
public class QuartzJobScanner {

    /**
     * 扫描指定包下所有带@QuartzJob注解的类
     */
    public static Set<BeanDefinition> scan(String basePackage) {
        // 创建类路径扫描器
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        // 添加注解过滤器（只扫描带@QuartzJob的类）
        scanner.addIncludeFilter(new AnnotationTypeFilter(Easy4jQzJobs.class));

        // 扫描指定包
        return scanner.findCandidateComponents(basePackage);
    }
}
