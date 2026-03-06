package com.floyd.sdk;

import com.alibaba.fastjson2.JSON;
import com.floyd.sdk.domain.model.ChatCompletionRequest;
import com.floyd.sdk.domain.model.ChatCompletionSyncResponse;
import com.floyd.sdk.domain.model.Model;
import com.floyd.sdk.types.utils.BearerTokenUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class OpenAiCodeReview {

    public final static String API_KEY_SECRET = "d37c4f500b504addbb5e265fc933daa0.czrCITvIzMuM9NNI";

    public final static String URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

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

        String log = codeReview(diffCode.toString());
        System.out.println("评审代码: " + log);
    }

    public static String codeReview(String code) throws IOException {
        String token = BearerTokenUtils.getToken(API_KEY_SECRET);

        URL url = new URL(URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // 为避免 code 的格式混乱，导致无法发送请求
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(Model.GLM_4_FLASH.getCode());
        request.setMessages(Arrays.asList(
                new ChatCompletionRequest.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言，请您根据git diff记录，对代码做出评审。代码为"),
                new ChatCompletionRequest.Prompt("user", code)
        ));

        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] bytes = JSON.toJSONString(request).getBytes(StandardCharsets.UTF_8);
            outputStream.write(bytes);
        }

        System.out.println(connection.getResponseCode());

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }

        ChatCompletionSyncResponse response = JSON.parseObject(content.toString(), ChatCompletionSyncResponse.class);
        return response.getChoices().get(0).getMessage().getContent();
    }

}
