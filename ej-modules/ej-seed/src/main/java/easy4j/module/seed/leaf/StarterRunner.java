package easy4j.module.seed.leaf;

import org.springframework.boot.CommandLineRunner;

import javax.annotation.Resource;

public class StarterRunner implements CommandLineRunner {

    @Resource
    private LeafGenIdService idGenService;

    @Override
    public void run(String... args) throws Exception {
        idGenService.init();
    }
}
