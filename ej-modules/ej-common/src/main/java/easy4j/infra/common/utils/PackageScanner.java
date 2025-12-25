package easy4j.infra.common.utils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 包扫描工具类：获取指定包下所有Java类
 */
public class PackageScanner {

    /**
     * 扫描指定包下的所有类（默认递归子包）
     * @param packageName 包名（如 com.example.demo）
     * @return 包下所有类的Class对象集合
     * @throws Exception 扫描/加载类异常
     */
    public static Set<Class<?>> scanPackage(String packageName) throws Exception {
        return scanPackage(packageName, true);
    }

    /**
     * 扫描指定包下的所有类
     * @param packageName 包名（如 com.example.demo）
     * @param recursive 是否递归扫描子包
     * @return 包下所有类的Class对象集合
     * @throws Exception 扫描/加载类异常
     */
    public static Set<Class<?>> scanPackage(String packageName, boolean recursive) throws Exception {
        Set<Class<?>> classSet = new LinkedHashSet<>();
        // 1. 包路径转资源路径（com.example.demo → com/example/demo）
        String packagePath = packageName.replace('.', '/');
        // 2. 获取类加载器（优先用当前线程上下文类加载器，兼容Web容器/框架）
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // 3. 获取包路径对应的所有资源（可能是目录、Jar包条目等）
        Enumeration<URL> resources = classLoader.getResources(packagePath);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            // 4. 解析资源协议（file: 本地文件；jar: Jar包内资源）
            String protocol = resource.getProtocol();
            if ("file".equals(protocol)) {
                // 场景1：本地文件系统（开发环境/未打包的类文件）
                String filePath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8.name());
                scanFilePackage(packageName, filePath, recursive, classSet);
            } else if ("jar".equals(protocol)) {
                // 场景2：Jar包内资源（生产环境/打包后的Jar）
                scanJarPackage(resource, packagePath, packageName, recursive, classSet);
            }
        }
        return classSet;
    }

    /**
     * 扫描本地文件系统中的包（处理 file: 协议）
     */
    private static void scanFilePackage(String packageName, String filePath, boolean recursive, Set<Class<?>> classSet) throws ClassNotFoundException {
        File dir = new File(filePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 遍历目录下的文件/子目录（过滤隐藏文件）
        File[] files = dir.listFiles(file -> 
            (file.isFile() && file.getName().endsWith(".class") && !file.getName().contains("$")) // 排除内部类
            || (recursive && file.isDirectory()) // 递归子目录
        );
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile()) {
                // 文件名转类名：如 Test.class → com.example.demo.Test
                String fileName = file.getName().substring(0, file.getName().length() - 6); // 去掉 .class
                String className = packageName + "." + fileName;
                // 加载类并加入集合（使用当前线程类加载器）
                Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                classSet.add(clazz);
            } else {
                // 递归扫描子包：如 com/example/demo/util → com.example.demo.util
                String subPackageName = packageName + "." + file.getName();
                scanFilePackage(subPackageName, file.getAbsolutePath(), recursive, classSet);
            }
        }
    }

    /**
     * 扫描Jar包中的包（处理 jar: 协议）
     */
    private static void scanJarPackage(URL resource, String packagePath, String packageName, boolean recursive, Set<Class<?>> classSet) throws IOException, ClassNotFoundException {
        JarURLConnection jarConn = (JarURLConnection) resource.openConnection();
        JarFile jarFile = jarConn.getJarFile();
        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName(); // 如 com/example/demo/Test.class

            // 1. 过滤目标包下的.class文件（排除目录、内部类）
            if (!entryName.startsWith(packagePath) || !entryName.endsWith(".class") || entryName.contains("$")) {
                continue;
            }
            // 2. 判断是否递归子包（非递归则只匹配当前包，不包含子包）
            if (!recursive) {
                String entryPackagePath = entryName.substring(0, entryName.lastIndexOf('/'));
                if (!entryPackagePath.equals(packagePath)) {
                    continue;
                }
            }
            // 3. Jar条目路径转类名：com/example/demo/Test.class → com.example.demo.Test
            String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
            // 4. 加载类并加入集合
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            classSet.add(clazz);
        }
        jarFile.close();
    }

    // --------------------- 简化版：仅获取类名（不加载Class） ---------------------
    /**
     * 扫描指定包下的所有类名（不加载类，性能更高）
     */
    public static Set<String> scanPackageForClassName(String packageName, boolean recursive) throws Exception {
        Set<String> classNameSet = new LinkedHashSet<>();
        String packagePath = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(packagePath);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();
            if ("file".equals(protocol)) {
                String filePath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8.name());
                scanFilePackageForClassName(packageName, filePath, recursive, classNameSet);
            } else if ("jar".equals(protocol)) {
                scanJarPackageForClassName(resource, packagePath, packageName, recursive, classNameSet);
            }
        }
        return classNameSet;
    }

    private static void scanFilePackageForClassName(String packageName, String filePath, boolean recursive, Set<String> classNameSet) {
        File dir = new File(filePath);
        if (!dir.exists() || !dir.isDirectory()) return;
        File[] files = dir.listFiles(file -> (file.isFile() && file.getName().endsWith(".class") && !file.getName().contains("$")) || (recursive && file.isDirectory()));
        if (files == null) return;
        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getName().substring(0, file.getName().length() - 6);
                classNameSet.add(packageName + "." + fileName);
            } else {
                String subPackageName = packageName + "." + file.getName();
                scanFilePackageForClassName(subPackageName, file.getAbsolutePath(), recursive, classNameSet);
            }
        }
    }

    private static void scanJarPackageForClassName(URL resource, String packagePath, String packageName, boolean recursive, Set<String> classNameSet) throws IOException {
        JarURLConnection jarConn = (JarURLConnection) resource.openConnection();
        JarFile jarFile = jarConn.getJarFile();
        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (!entryName.startsWith(packagePath) || !entryName.endsWith(".class") || entryName.contains("$")) continue;
            if (!recursive) {
                String entryPackagePath = entryName.substring(0, entryName.lastIndexOf('/'));
                if (!entryPackagePath.equals(packagePath)) continue;
            }
            String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
            classNameSet.add(className);
        }
        jarFile.close();
    }

    // 测试示例
    public static void main(String[] args) throws Exception {
        // 示例1：扫描指定包下的所有Class对象（递归子包）
        String packageName = "easy4j.infra.common.utils";
        Set<Class<?>> classSet = scanPackage(packageName,false);
        System.out.println("===== 扫描到的类（Class对象） =====");
        for (Class<?> clazz : classSet) {
            System.out.println(clazz.getSimpleName());
        }

        // 示例2：仅扫描类名（不加载类）
        Set<String> classNameSet = scanPackageForClassName(packageName, false); // 不递归子包
        System.out.println("\n===== 扫描到的类名（仅当前包） =====");
        for (String className : classNameSet) {
            System.out.println(className);
        }
    }
}