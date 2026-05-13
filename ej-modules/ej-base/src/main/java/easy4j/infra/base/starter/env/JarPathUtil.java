package easy4j.infra.base.starter.env;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.SysLog;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * JAR 包路径工具类（修复版）
 * 用于在运行时获取当前 JAR 包所在的路径
 */
public class JarPathUtil {
    private static String path;

    static {
        try{
            URL url = JarPathUtil.class.getProtectionDomain().getCodeSource().getLocation();
            path = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
            // nested:/E:/xxx.jar/!BOOT-INF/lib/xxx.jar!/
            if (path.startsWith("nested:")) {
                // nested:/E:/path/app.jar/!BOOT-INF/lib/xxx.jar!/
                // 提取第一个 .jar 前的路径
                path = path.substring(7); // 移除 "nested:" 前缀
                int jarIndex = path.indexOf(".jar");
                if (jarIndex > 0) {
                    path = path.substring(0, jarIndex + 4);
                }
            }
            // Windows 路径处理
            if (path.startsWith("/") && path.length() > 2 && path.charAt(2) == ':') {
                path = path.substring(1);
            }
            System.out.println(SysLog.compact("app runtime path is ->" + path));
        }catch (Exception e){
            System.out.println(SysLog.compact("static get jar directory error " + e.getMessage()));
        }

    }

    /**
     * 获取当前 JAR 包所在的目录
     *
     * @return JAR 包所在目录的绝对路径
     */
    public static String getJarDirectory() {
        try {

            File file = new File(path);

            // 如果是 JAR 文件，返回其所在目录
            if (file.isFile() && path.endsWith(".jar")) {
                return file.getParent();
            }

            // 如果是目录（开发环境），直接返回
            return path;
        } catch (Exception e) {
            System.out.println(SysLog.compact("get jar directory error " + e.getMessage()));
            return null;
        }

    }

    /**
     * 获取当前 JAR 包的完整路径
     *
     * @return JAR 包的绝对路径
     */
    public static String getJarPath() {
        URL url = JarPathUtil.class.getProtectionDomain().getCodeSource().getLocation();
        String path = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);

        // Windows 环境下，移除开头的 /
        if (path.startsWith("/") && path.contains(":")) {
            path = path.substring(1);
        }

        File file = new File(path);

        // 如果是 JAR 文件，返回完整路径
        if (file.isFile() && path.endsWith(".jar")) {
            return path;
        }

        // 开发环境返回目录
        return path;
    }

    /**
     * 获取 JAR 包的名称
     *
     * @return JAR 包名称（仅包含文件名）
     */
    public static String getJarName() {
        String path = getJarPath();
        return new File(path).getName();
    }

    /**
     * 获取 JAR 所在目录下指定子路径的绝对路径
     *
     * @param relativePath 相对于 JAR 目录的相对路径
     * @return 绝对路径
     */
    public static String getAbsolutePath(String relativePath) {
        String jarDir = getJarDirectory();

        // 使用 File 代替 Paths 来构建路径（更兼容 Windows）
        File baseDir = new File(jarDir);
        File targetFile = new File(baseDir, relativePath);

        return targetFile.getAbsolutePath();
    }

    /**
     * 检查当前是否运行在 JAR 包中
     *
     * @return true 表示运行在 JAR 中，false 表示运行在开发环境
     */
    public static boolean isRunningInJar() {
        String path = getJarPath();
        return path.endsWith(".jar");
    }

    /**
     * 打印调试信息
     */
    public static void printDebugInfo() {
        System.out.println("======== JAR 路径信息 ========");
        System.out.println("JAR 完整路径: " + getJarPath());
        System.out.println("JAR 所在目录: " + getJarDirectory());
        System.out.println("JAR 文件名: " + getJarName());
        System.out.println("运行环境: " + (isRunningInJar() ? "JAR 包" : "开发环境"));
        System.out.println("操作系统: " + System.getProperty("os.name"));
        System.out.println("==============================");
    }

    // 使用示例
//    public static void main(String[] args) {
//        // 打印调试信息
//        printDebugInfo();
//
//        // 获取配置文件路径
//        String configPath = getAbsolutePath("config/application.properties");
//        System.out.println("配置文件路径: " + configPath);
//
//        // 获取日志目录
//        String logDir = getAbsolutePath("logs");
//        System.out.println("日志目录: " + logDir);
//    }
}
