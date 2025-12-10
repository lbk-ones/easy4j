/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.infra.rpc.serializable;

import easy4j.infra.rpc.domain.FilterAttributes;
import easy4j.infra.rpc.domain.RpcRequest;
import easy4j.infra.rpc.domain.RpcResponse;
import easy4j.infra.rpc.domain.Transport;
import easy4j.infra.rpc.integrated.spring.BeanImport;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;


/**
 * 管理要序列化的类信息 给 hession和kryo用
 *
 * @author bokun
 * @since 2.0.1
 */
@Slf4j
public class SerializerSecurityRegistry {
    private static final Set<Class<?>> ALLOW_CLAZZ_SET = new HashSet<>();

    private static final Set<String> ALLOW_CLAZZ_PATTERN = new HashSet<>();

    private static final Set<String> DENY_CLAZZ_PATTERN = new HashSet<>();

    private static final String CLASS_POSTFIX = ".class";

    private static final String ABSTRACT_CLASS_ID = "Abstract";

    /**
     * 传输类的命名规范（结尾）
     */
    private static final String[] END_WITH = new String[]{"Request", "Req", "Response", "Res", "Message", "Dto", "DTO", "Domain"};

    static {
        // 基础类型
        ALLOW_CLAZZ_SET.addAll(Arrays.asList(getBasicClassType()));
        // 集合
        ALLOW_CLAZZ_SET.addAll(Arrays.asList(getCollectionClassType()));
        // 动态扫描出来的
        ALLOW_CLAZZ_SET.addAll(getScanTypes());
        // 内定的传输类
        ALLOW_CLAZZ_SET.addAll(Arrays.asList(getProtocolInnerFields()));
        for (Class<?> clazz : ALLOW_CLAZZ_SET) {
            ALLOW_CLAZZ_PATTERN.add(clazz.getCanonicalName());
        }
        ALLOW_CLAZZ_PATTERN.add(getSelfClassPattern());
        BeanImport.getDomainPackages().stream().map(e -> e + ".*").forEach(ALLOW_CLAZZ_PATTERN::add);
        DENY_CLAZZ_PATTERN.addAll(Arrays.asList(getDenyClassPatternList()));
    }

    public static Set<Class<?>> getAllowClassType() {
        return Collections.unmodifiableSet(ALLOW_CLAZZ_SET);
    }

    public static Set<String> getAllowClassPattern() {
        return Collections.unmodifiableSet(ALLOW_CLAZZ_PATTERN);
    }

    public static Set<String> getDenyClassPattern() {
        return Collections.unmodifiableSet(DENY_CLAZZ_PATTERN);
    }

    private static Class<?>[] getBasicClassType() {
        return new Class[]{Boolean.class, Byte.class, Character.class, Double.class, Float.class, Integer.class,
                Long.class, Short.class, Number.class, Class.class, String.class};
    }

    private static Class<?>[] getCollectionClassType() {
        return new Class[]{ArrayList.class, LinkedList.class, HashSet.class,
                LinkedHashSet.class, TreeSet.class, HashMap.class, LinkedHashMap.class, TreeMap.class};
    }

    private static String getSelfClassPattern() {
        return "easy4j.*";
    }

    private static String[] getDenyClassPatternList() {
        return new String[]{"javax.naming.InitialContext", "javax.net.ssl.*", "com.unboundid.ldap.*", "java.lang.Runtime"};
    }

    private static Set<Class<?>> getScanTypes() {
        Set<Class<?>> classNameSet = new HashSet<>();

        try {
            List<String> basePackages = BeanImport.getDomainPackages();
            for (String packageName : basePackages) {
                Enumeration<URL> packageDir = Thread.currentThread().getContextClassLoader().getResources(packageName.replace(".", "/"));
                while (packageDir.hasMoreElements()) {
                    String filePath = packageDir.nextElement().getFile();
                    findProtocolClassByPackage(filePath, packageName, classNameSet);
                }
            }

        } catch (IOException ignore) {
        }
        log.info("serializer security registry dynamic scan "+classNameSet.size()+" domains");

        return classNameSet;
    }

    private static void findProtocolClassByPackage(String classPath, String rootPackageName, Set<Class<?>> classNameSet) {
        File file = new File(classPath);
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null == files) {
                return;
            }
            for (File path : files) {
                if (path.isDirectory()) {
                    findProtocolClassByPackage(path.getAbsolutePath(), rootPackageName + "." + path.getName(),
                            classNameSet);
                } else {
                    findProtocolClassByPackage(path.getAbsolutePath(), rootPackageName, classNameSet);
                }
            }
        } else {
            if (matchProtocol(file.getName())) {
                String className = file.getName().substring(0, file.getName().length() - CLASS_POSTFIX.length());
                try {
                    classNameSet.add(
                            Thread.currentThread().getContextClassLoader().loadClass(rootPackageName + '.' + className));
                } catch (ClassNotFoundException ignore) {
                    //ignore interface
                }
            }
        }
    }

    private static boolean matchProtocol(String fileName) {
        if (!fileName.endsWith(CLASS_POSTFIX)) {
            return false;
        }
        fileName = fileName.replace(CLASS_POSTFIX, "");
        if (fileName.startsWith(ABSTRACT_CLASS_ID)) {
            return false;
        }
        for (String s : END_WITH) {
            if (fileName.endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    private static Class<?>[] getProtocolInnerFields() {
        return new Class<?>[]{RpcResponse.class, RpcRequest.class, FilterAttributes.class, Transport.class};
    }
}
