package com.fdurjw.llmmapperweb.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class MapperGenerateService {

    /**
     * 1. chmod +x generate.sh
     * 2. make generate (在脚本或其他方式中输入deviceName)
     * 3. 打包 kubeedge/staging/src/github.com/kubeedge/<deviceName> 文件夹
     * 4. 返回zip文件的路径
     */
    public Path generateMapperCode(String deviceName) throws IOException, InterruptedException {

        // 1. 赋予 generate.sh 脚本可执行权限
        chmodGenerateScript();

        // 2. 在 mapper-framework 目录下执行 make generate
        makeGenerate(deviceName);

        // 3. 打包指定的 mapper 目录到 zip
        System.out.println("Zipping files...");
        // 获取当前工作目录（即项目根目录）
        String projectRoot = System.getProperty("user.dir");

        // 构造跨平台的路径
        Path folderToZip = Paths.get(projectRoot, "kubeedge", deviceName);
        Path zipPath = Paths.get(projectRoot, "mappers", deviceName + "_mapper.zip");

        // 打印路径以确保正确性
        System.out.println("Folder to zip: " + folderToZip);
        System.out.println("Zip path: " + zipPath);

        zipFolder(folderToZip, zipPath);

        // 4. 返回压缩文件路径，用于后续更新数据库
        return zipPath;
    }

    public void chmodGenerateScript() throws IOException, InterruptedException {
        // 获取当前工作目录（即项目根目录）
        // 获取当前工作目录（即项目根目录）
        String projectRoot = System.getProperty("user.dir");

        // 构造跨平台路径
        Path generateScriptPath = Paths.get(projectRoot, "kubeedge", "mapper-framework", "hack", "make-rules", "generate.sh");

        // 使用 sudo -S 选项，通过标准输入传递密码
        String command = "sudo -S chmod +x " + generateScriptPath.toString();

        // 输出命令以确保路径正确
        System.out.println("Executing command: " + command);
        // 通过 ProcessBuilder 执行命令
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        pb.redirectErrorStream(true);

        // 启动进程并获取输出流
        Process process = pb.start();

        // 向 sudo 进程提供密码，通过标准输入流提供密码
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
            // 输入正确的 sudo 密码
            writer.write("04241029Bb"); // 请确保密码是正确的
            writer.newLine();  // 加上换行符模拟按回车键
            writer.flush();
        }

        // 读取并打印命令输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[chmod output] " + line);
            }
        }

        // 等待进程执行完成并检查退出状态
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to chmod generate.sh, exit code = " + exitCode);
        }
    }

    public void makeGenerate(String deviceName) throws IOException, InterruptedException {
        // 1. 在 mapper-framework 目录下执行 make generate
        ProcessBuilder pb = new ProcessBuilder("make", "generate");
        // 获取当前工作目录（即项目根目录）
        String projectRoot = System.getProperty("user.dir");

        // 使用 Paths.get() 构造跨平台路径
        Path targetDir = Paths.get(projectRoot, "kubeedge", "mapper-framework");

        // 输出目标目录的路径以确保正确
        System.out.println("Target directory: " + targetDir);

        // 设置 ProcessBuilder 的工作目录为相对路径
        pb.directory(targetDir.toFile());
        // 将错误流合并到标准输出流，以便一起读取
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // 2. 如果脚本/命令需要从标准输入读入 deviceName，就将 deviceName 写入 process.getOutputStream()
        try (BufferedWriter writer =
                     new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
            writer.write(deviceName);
            writer.newLine();  // 等效于手动按了一次回车
            writer.flush();
        }

        // 3. 读取并打印执行过程的输出，并捕获 sed 错误输出
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 如果是 sed 错误，忽略它
                if (line.contains("command a expects \\ followed by text")) {
                    System.out.println("[make generate output] Ignoring sed error: " + line);
                } else {
                    System.out.println("[make generate output] " + line);
                }
            }
        }

        // 4. 等待子进程执行结束，检查状态码
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            // 仅忽略特定的错误（如 sed 错误），其他错误仍然会抛出异常
            if (exitCode == 2) {
                // `make` 错误代码为 2，忽略错误并继续执行后续操作
                System.out.println("make generate exit code 2. Ignoring this error and continuing.");
            } else {
                // 如果是其他类型的错误，抛出异常并停止
                System.out.println("Warning: make generate failed with exit code " + exitCode);
                throw new RuntimeException("Failed to execute make generate, exit code = " + exitCode);
            }
        }
    }

    /**
     * 将指定文件夹压缩成zip
     *
     * @param sourceDirPath 待打包的文件夹路径
     * @param zipFilePath   生成的zip文件路径
     */
    private void zipFolder(Path sourceDirPath, Path zipFilePath) throws IOException {
        // 如果父级目录不存在需要先创建
        Files.createDirectories(zipFilePath.getParent());

        try (ZipOutputStream zs = new ZipOutputStream(new FileOutputStream(zipFilePath.toFile()))) {
            // walk遍历文件并过滤掉目录本身
            Files.walk(sourceDirPath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException("Error zipping file: " + path, e);
                        }
                    });
        }
    }
}