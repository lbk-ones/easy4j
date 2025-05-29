package easy4j.module.seed.leaf;

import easy4j.module.base.context.Easy4jContext;
import easy4j.module.base.plugin.seed.Easy4jSeed;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.seed.CommonKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

import javax.annotation.Resource;

/**
 * StarterRunner
 *
 * @author bokun.li
 * @date 2025-05
 */
@Slf4j
public class StarterRunner implements CommandLineRunner {

    @Resource
    private LeafGenIdService idGenService;

    @Override
    public void run(String... args) throws Exception {

        Easy4jContext context = Easy4j.getContext();
        context.set(Easy4jSeed.class, CommonKey.getCommonKey());
        
        idGenService.init();
    }
}