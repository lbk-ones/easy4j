package ej.spring.boot.nacos;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class NacosServerStarter implements CommandLineRunner {

    @Value("${nacos.version:2.2.0}")
    private String nacosVersion;

    // https://github.com/alibaba/nacos/releases/download/2.2.0/nacos-server-2.2.0.tar.gz
    @Value("${nacos.download.url:https://github.com/alibaba/nacos/releases/download/${nacos.version}/nacos-server-${nacos.version}.zip}")
    private String downloadUrl;

    @Value("${nacos.data.dir:./nacos-data}")
    private String dataDir;

    @Value("${nacos.port:8849}")
    private int port;

    private Process nacosProcess;

    @Override
    public void run(String... args) throws Exception {
        // 确保数据目录存在
        Path dataPath = Paths.get(dataDir);
        if (!Files.exists(dataPath)) {
            Files.createDirectories(dataPath);
        }

        // 下载Nacos JAR（如果不存在）
        Path nacosZipPath = Paths.get(dataDir, "nacos-server-" + nacosVersion + ".zip");
        if (!Files.exists(nacosZipPath)) {
            downloadNacos(nacosZipPath);
        }

        // 解压JAR（如果未解压）
        Path nacosDir = Paths.get(dataDir, "nacos");
        if (!Files.exists(nacosDir)) {
            unzipNacos(nacosZipPath, dataDir);
        }

        // 启动Nacos
        startNacos(nacosDir);

        // 添加JVM关闭钩子，确保优雅停止
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopNacos));
    }

    private void downloadNacos(Path targetPath) throws IOException {
        System.out.println("正在下载Nacos Server: " + downloadUrl);
        try (var in = new URL(downloadUrl).openStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("Nacos下载完成: " + targetPath);
    }

    private void unzipNacos(Path zipPath, String destDir) throws IOException, InterruptedException {
        System.out.println("正在解压Nacos Server...");
        String string = zipPath.toString();
        ZipExtractor.unzip(string, destDir);
//        Process unzipProcess = Runtime.getRuntime().exec(
//                String.format("unzip -o %s -d %s", zipPath, destDir)
//        );
//        int exitCode = unzipProcess.waitFor();
//        if (exitCode != 0) {
//            throw new IOException("解压Nacos失败，退出码: " + exitCode);
//        }
        System.out.println("Nacos解压完成");
    }

    private void startNacos(Path nacosDir) throws IOException {
        System.out.println("正在启动Nacos Server...");

        // 构建启动命令（根据操作系统选择）
        String os = System.getProperty("os.name").toLowerCase();
        String command;
        if (os.contains("win")) {
            command = String.format("cmd /c start %s\\bin\\startup.cmd -m standalone", nacosDir);
        } else {
            command = String.format("sh %s/bin/startup.sh -m standalone", nacosDir);
        }

        // 执行命令
        nacosProcess = Runtime.getRuntime().exec(command);

        System.out.println("Nacos Server已启动，访问: http://localhost:" + port + "/nacos");
    }

    private void stopNacos() {
        System.out.println("正在停止Nacos Server...");
        if (nacosProcess != null && nacosProcess.isAlive()) {
            nacosProcess.destroy();
            try {
                if (nacosProcess.waitFor(10, java.util.concurrent.TimeUnit.SECONDS)) {
                    System.out.println("Nacos Server已停止");
                } else {
                    nacosProcess.destroyForcibly();
                    System.out.println("Nacos Server已强制停止");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("停止Nacos Server时被中断");
            }
        }
    }
}