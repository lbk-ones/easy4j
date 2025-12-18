/*
 * Copyright © 2018 organization baomidou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.datasource.dynamic;


import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
/**
 * Core Interceptor of Dynamic Datasource
 *
 * @author TaoYu
 * @since 1.2.0
 */
public class DynamicDataSourceAnnotationInterceptor implements MethodInterceptor {

    /**
     * The identification of SPEL.
     */
    private static final String DYNAMIC_PREFIX = "#";
    private final DynamicDataSourceClassResolver dynamicDataSourceClassResolver;

    public DynamicDataSourceAnnotationInterceptor(Boolean allowedPublicOnly) {
        dynamicDataSourceClassResolver = new DynamicDataSourceClassResolver(allowedPublicOnly);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String dsKey = determineDatasourceKey(invocation);
        DataSourceContextHolder.setDataSourceKey(dsKey);
        try {
            return invocation.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceKey();
        }
    }

    /**
     * Determine the key of the datasource
     *
     * @param invocation MethodInvocation
     * @return dsKey
     */
    private String determineDatasourceKey(MethodInvocation invocation) {
        return dynamicDataSourceClassResolver.findKey(invocation.getMethod(), invocation.getThis(), DataSource.class);
    }

}