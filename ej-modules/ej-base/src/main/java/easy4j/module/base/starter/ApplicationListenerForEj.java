/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
