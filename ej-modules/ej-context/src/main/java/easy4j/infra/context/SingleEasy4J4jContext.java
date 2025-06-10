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
package easy4j.infra.context;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.common.exception.EasyException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SingleEasy4J4jContext
 * 单例上下文
 *
 * @author bokun.li
 * @date 2025/6/10
 */
class SingleEasy4J4jContext extends ThreadEasy4jContext implements Easy4jContext {


    // 单例
    private static final Map<String, Object> singleMap = new ConcurrentHashMap<>(256);

    // 类型对于单例
    private static final Map<Class<?>, String[]> classMap = new ConcurrentHashMap<>(64);

    private static final Object lock = new Object();


    private void clear() {
        singleMap.clear();
        classMap.clear();
    }


    /**
     * 注册对象
     * 分析这个对象所有继承关系 接口关系
     * 然后把这些关系与对象class的defaultName做映射
     * defaultName生成规则 class对象SimpleName首字母小写
     *
     * @author bokun.li
     * @date 2025/6/10
     */
    public void register(Object object) {
        if (object != null) {
            synchronized (lock) {
                Class<?> aClass = object.getClass();
                String defaultName = StrUtil.lowerFirst(aClass.getSimpleName());
                // lambda and Anonymous class and inner class maybe defaultName is ""
                if (StrUtil.isEmpty(defaultName)) {
                    defaultName = aClass.getName();
                }
                Object o = singleMap.get(defaultName);
                // not allow repeat register
                // quietly
                if (Objects.nonNull(o)) {
                    return;
                }
                singleMap.put(defaultName, object);
                putClassName(aClass, defaultName);
                Set<Class<?>> superclasses = getAllSuperclasses(object);
                for (Class<?> superclass1 : superclasses) {
                    putClassName(superclass1, defaultName);
                }
                Set<Class<?>> interfaces = getAllInterfaces(object);
                for (Class<?> anInterface : interfaces) {
                    putClassName(anInterface, defaultName);
                }
            }
        }
    }


    /**
     * 获取对象的所有父类（不包含 Object）
     *
     * @param object 目标对象
     * @return 父类 Class 集合（直接父类在前，祖先类在后）
     */
    public static Set<Class<?>> getAllSuperclasses(Object object) {
        Set<Class<?>> superclasses = new LinkedHashSet<>();
        Class<?> clazz = object.getClass();

        // 遍历父类链，直到 Object 类（不包含 Object）
        while ((clazz = clazz.getSuperclass()) != null && !clazz.equals(Object.class)) {
            superclasses.add(clazz);
        }
        return superclasses;
    }

    /**
     * 获取对象实现的所有接口（包括父接口）
     *
     * @param object 目标对象
     * @return 接口 Class 集合（包含当前类直接实现的接口及其所有父接口）
     */
    public static Set<Class<?>> getAllInterfaces(Object object) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        Class<?> clazz = object.getClass();

        // 添加当前类直接实现的接口
        addInterfaces(clazz, interfaces);

        // 递归添加父类实现的接口
        while ((clazz = clazz.getSuperclass()) != null && !clazz.equals(Object.class)) {
            addInterfaces(clazz, interfaces);
        }
        return interfaces;
    }

    /**
     * 递归添加一个类及其父接口的所有接口
     */
    private static void addInterfaces(Class<?> clazz, Set<Class<?>> interfaces) {
        // 添加当前类实现的所有接口
        for (Class<?> iface : clazz.getInterfaces()) {
            interfaces.add(iface);
            // 递归添加接口的父接口
            addInterfaces(iface, interfaces);
        }
    }


    public void register(String name, Object object) {
        if (object != null) {
            synchronized (lock) {
                register(object);
                // 如果传入的name和默认名称不一样 那么再单独放一份
                String defaultName = StrUtil.lowerFirst(object.getClass().getSimpleName());
                if (!StrUtil.equals(defaultName, name)) {
                    Object o = singleMap.get(name);
                    if (o != null) {
                        throw new EasyException("This " + name + " has already been registered and is in use.");
                    }
                    singleMap.put(name, object);
                }
            }
        }
    }


    private void putClassName(Class<?> aClass, String s) {
        String[] strings = classMap.get(aClass);
        String[] newStrArray;
        if (strings != null && strings.length > 0) {
            newStrArray = Arrays.copyOf(strings, strings.length + 1);
            newStrArray[newStrArray.length - 1] = s;
        } else {
            newStrArray = new String[]{s};
        }
        classMap.put(aClass, newStrArray);
    }

    /**
     * 如果当前上下文还没来得及加载那么手动去加载（这种很正常有可能再使用当前上下文的时候这个时候太早了导致没有加载）
     * 根据类型去匹配，如果一个类型有好几个实现默认名称会优先匹配
     * 同时defaultName也要匹配上 如果匹配补上 那么这个类型只有一个那也可以直接拿
     * 如果最后还是拿不到会从上下文插件链中去拿取
     *
     * @param aclass
     * @param <T>
     * @return
     * @throws EasyException
     */
    public <T> T get(Class<T> aclass) {

        if (aclass == null) return null;

        synchronized (lock) {
            String[] o1 = classMap.get(aclass);

            String defaultName = StrUtil.lowerFirst(aclass.getSimpleName());
            if (o1 == null) {
                T call = ContextPlugins.call(defaultName, aclass);
                if (call != null) {
                    register(call);
                }
                o1 = classMap.get(aclass);
                if (o1 == null) {
                    throw new EasyException("not find " + aclass.getName() + " from context!");
                }
            }

            for (String s : o1) {
                Object o = singleMap.get(s);
                if (StrUtil.equals(defaultName, s)) {
                    return aclass.cast(o);
                }
            }
            Object o = null;
            try {
                if (o1.length == 1) {
                    String s = o1[0];
                    o = singleMap.get(s);
                    T t = Optional.ofNullable(o)
                            .map(aclass::cast)
                            .orElseGet(() -> ContextPlugins.call(defaultName, aclass));
                    if (t == null) {
                        throw new EasyException("not find " + aclass.getName() + " from context!");
                    }
                    return t;
                } else {
                    throw new EasyException("not find " + aclass.getName() + " from context! Because there are multiple instances " + Arrays.toString(o1));
                }
            } catch (ClassCastException e) {
                if (o != null) {
                    throw new EasyException("not cast to " + aclass.getName() + " from type " + o.getClass().getName());
                } else {
                    throw new EasyException("not cast to " + aclass.getName());
                }
            }

        }
    }

    public <T> T get(String name, Class<T> aClass) {
        if (null == aClass) {
            throw new EasyException("aClass is not allow null!");
        }
        synchronized (lock) {
            Object o = singleMap.get(name);
            T t = null;
            try {
                t = Optional.ofNullable(o)
                        .map(aClass::cast)
                        .orElseGet(() -> {
                            T call = ContextPlugins.call(name, aClass);
                            if (call != null) {
                                register(call);
                            }
                            return call;
                        });

            } catch (ClassCastException e) {
                if (o != null) {
                    throw new EasyException("not cast to " + aClass.getName() + " from type " + o.getClass().getName());
                }
            }
            if (t == null) {
                throw new EasyException("not find " + name + "from context!");
            }
            return t;
        }


    }

    public Object get(String name) {
        synchronized (lock) {
            Object o = Optional.ofNullable(singleMap.get(name)).orElseGet(() -> {
                Object call = ContextPlugins.call(name, null);
                if (call != null) {
                    register(call);
                }
                return call;
            });

            if (o == null) {
                throw new EasyException("not find " + name + " from context!");
            }
            return o;
        }
    }

    @Override
    public <T> Map<String, T> getMapOfType(Class<T> tClass) {
        Map<String, T> res = Maps.newHashMap();
        Optional.ofNullable(classMap.get(tClass)).ifPresent(e -> {
            for (String s : e) {
                Object o = singleMap.get(s);
                if (o != null) {
                    res.putIfAbsent(s, tClass.cast(o));
                }
            }
        });
        return res;
    }

    @Override
    public <T> String[] getNamesOfType(Class<T> tClass) {
        return Optional.ofNullable(classMap.get(tClass)).orElse(new String[]{});
    }
}
