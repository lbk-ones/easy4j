package easy4j.module.base.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.time.Duration;

/**
 * ApplicationListenerForEj
 *
 * @author bokun.li
 * @date 2025-05
 */
public interface ApplicationListenerForEj {

    /**
     * 构造
     */
    default void construct(SpringApplication application, String[]  args){

    }
    
    /**
     * {@link ApplicationRunListenerForEj#starting}.
     */
    default void starting() {
    }
    
    /**
     * {@link ApplicationRunListenerForEj#environmentPrepared}.
     *
     * @param environment environment
     */
    default void environmentPrepared(ConfigurableEnvironment environment) {
    }
    
    /**
     * {@link ApplicationRunListenerForEj#contextPrepared}.
     *
     * @param context context
     */
    default void contextPrepared(ConfigurableApplicationContext context) {
    }
    
    /**
     * {@link ApplicationRunListenerForEj#contextLoaded}.
     *
     * @param context context
     */
    default void contextLoaded(ConfigurableApplicationContext context) {
    }
    
    /**
     * {@link ApplicationRunListenerForEj#started}.
     *
     * @param context context
     */
    default void started(ConfigurableApplicationContext context, Duration timeTaken) {
    }
    
    /**
     * {@link ApplicationRunListenerForEj#ready}.
     *
     * @param context context
     * @param timeTaken timeTaken
     */
    default void ready(ConfigurableApplicationContext context, Duration timeTaken) {
    }
    
    /**
     * {@link ApplicationRunListenerForEj#failed}.
     *
     * @param context   context
     * @param exception exception
     */
    default void failed(ConfigurableApplicationContext context, Throwable exception) {
    }
}