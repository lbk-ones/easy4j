package easy4j.infra.quartz.demo;

import org.springframework.stereotype.Service;

@Service
public class DemoService {
    public void doSomething() {
        System.out.println("DemoService执行业务逻辑");
    }
}
