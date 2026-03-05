package com.floyd.sdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class OpenAiCodeReview {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("测试执行");

        // 1.代码检出：基于 Github action 环境，在 Java 程序中调用 Git 命令，获取当前仓库最近一次提交的代码变更内容
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        // 设置工作目录为当前项目根目录，告诉 Git 命令在哪个文件夹下执行
        ProcessBuilder directory = processBuilder.directory(new File("."));

        Process process = directory.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        StringBuilder diffCode = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            diffCode.append(line);
        }

        int exitCode = process.waitFor();
        System.out.println("Exited with code: " + exitCode);
        System.out.println("评审代码: " + diffCode);
    }

}
