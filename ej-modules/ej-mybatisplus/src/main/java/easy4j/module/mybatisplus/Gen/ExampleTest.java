package easy4j.module.mybatisplus.Gen;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import easy4j.infra.common.annotations.Desc;

import java.nio.file.Paths;

@Desc("测试mybatis代码生成")
public class ExampleTest {
    public static void main(String[] args) {

        String s = Paths.get(System.getProperty("user.dir")) + "/ej-modules/ej-mybatisplus/src/main/java";
        System.out.println(s);

        FastAutoGenerator.create("jdbc:mysql://localhost:3306/vcc_portal_v1", "root", "123456")
                .globalConfig(builder -> builder
                        .author("bokun.li")
                        .outputDir(s)
                        .commentDate("yyyy-MM-dd")
                        .enableSpringdoc()
                )
                .packageConfig(builder -> builder
                        .parent("easy4j.module.mybatisplus")
                        .entity("domains")
                        .controller("controller")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .xml("mapper.xml")
                )
                .strategyConfig(builder -> builder
                        .addExclude("flyway_schema_history_portal", "key_idempotent", "leaf_alloc", "sys_log_record")
                        .addTablePrefix("tb_")// 将 tb_前缀去掉
                        .entityBuilder()
                        .columnNaming(NamingStrategy.underline_to_camel)
                        .enableLombok()
                        .controllerBuilder()
                        //.disable() // 不生成 controller
                        .enableRestStyle() // 启用 REST 风格
                )
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
