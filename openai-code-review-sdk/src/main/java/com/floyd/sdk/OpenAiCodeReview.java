package com.floyd.sdk;

import com.alibaba.fastjson2.JSON;
import com.floyd.sdk.domain.model.Model;
import com.floyd.sdk.domain.service.impl.OpenAiCodeReviewService;
import com.floyd.sdk.infrastructure.git.GitCommand;
import com.floyd.sdk.infrastructure.openai.IOpenAI;
import com.floyd.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import com.floyd.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;
import com.floyd.sdk.infrastructure.openai.impl.ChatGLM;
import com.floyd.sdk.infrastructure.weixin.Weixin;
import com.floyd.sdk.infrastructure.weixin.dto.TemplateMessageDTO;
import com.floyd.sdk.types.utils.BearerTokenUtils;
import com.floyd.sdk.types.utils.WXAccessTokenUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

public class OpenAiCodeReview {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiCodeReview.class);

    public static void main(String[] args) {
        GitCommand gitCommand = new GitCommand(
                getEnv("CODE_REVIEW_LOG_URI"),
                getEnv("CODE_TOKEN"),
                getEnv("COMMIT_PROJECT"),
                getEnv("COMMIT_BRANCH"),
                getEnv("COMMIT_AUTHOR"),
                getEnv("COMMIT_MESSAGE")
        );

        /**
         * 项目：{{repo_name.DATA}} 分支：{{branch_name.DATA}} 作者：{{commit_author.DATA}} 说明：{{commit_message.DATA}}
         */
        Weixin weiXin = new Weixin(
                getEnv("WEIXIN_APPID"),
                getEnv("WEIXIN_SECRET"),
                getEnv("WEIXIN_TOUSER"),
                getEnv("WEIXIN_TEMPLATE_ID")
        );

        IOpenAI openAI = new ChatGLM(getEnv("CHATGLM_APIHOST"), getEnv("CHATGLM_APIKEYSECRET"));

        OpenAiCodeReviewService openAiCodeReviewService = new OpenAiCodeReviewService(gitCommand, openAI, weiXin);
        openAiCodeReviewService.exec();
        logger.info("openai-code-review done!");
    }

    private static String getEnv(String key) {
        String value = System.getenv(key);
        if (null == value || value.isEmpty()) {
            throw new RuntimeException(String.format("value is null: %s", key));
        }
        return value;
    }

    public final static String API_KEY_SECRET = "d37c4f500b504addbb5e265fc933daa0.czrCITvIzMuM9NNI";

    public final static String URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

//    public static void main(String[] args) throws IOException, InterruptedException, GitAPIException {
//        System.out.println("openai 代码评审，测试执行");
//
//        String token = System.getenv("GITHUB_TOKEN");
//        if (null == token || token.isEmpty()) {
//            throw new RuntimeException("token is null");
//        }
//
//        // 1.代码检出：基于 Github action 环境，在 Java 程序中调用 Git 命令，获取当前仓库最近一次提交的代码变更内容
//        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
//        // 设置工作目录为当前项目根目录，告诉 Git 命令在哪个文件夹下执行
//        ProcessBuilder directory = processBuilder.directory(new File("."));
//
//        Process process = directory.start();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//        String line;
//
//        StringBuilder diffCode = new StringBuilder();
//        while ((line = reader.readLine()) != null) {
//            diffCode.append(line);
//        }
//
//        int exitCode = process.waitFor();
//        System.out.println("Exited with code: " + exitCode);
//
//        String log = codeReview(diffCode.toString());
//        System.out.println("评审代码: " + log);
//
//        String logUrl = writeLog(token, log);
//        System.out.println("writeLog：" + logUrl);
//
//        System.out.println("正在推送消息");
//        pushMessage(logUrl);
//    }

    public static String codeReview(String code) throws IOException {
        String token = BearerTokenUtils.getToken(API_KEY_SECRET);

        URL url = new URL(URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // 为避免 code 的格式混乱，导致无法发送请求
        ChatCompletionRequestDTO request = new ChatCompletionRequestDTO();
        request.setModel(Model.GLM_4_FLASH.getCode());
        request.setMessages(Arrays.asList(
                new ChatCompletionRequestDTO.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言，请您根据git diff记录，对代码做出评审。代码为"),
                new ChatCompletionRequestDTO.Prompt("user", code)
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

        ChatCompletionSyncResponseDTO response = JSON.parseObject(content.toString(), ChatCompletionSyncResponseDTO.class);
        return response.getChoices().get(0).getMessage().getContent();
    }

    public static String writeLog(String token, String log) throws GitAPIException, IOException {
        Date currentDate = new Date();

        // 克隆仓库至 repo 文件夹
        Git git = Git.cloneRepository()
                .setURI("https://github.com/burn1t/openai-code-review-log.git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""))
                .call();

        // 创建日期标识的文件夹，并创建日志文件
        String logFolderName = new SimpleDateFormat("yyyy-MM-dd").format(currentDate);
        File logFolder = new File("repo/" + logFolderName);
        if (!logFolder.exists()) {
            logFolder.mkdirs();
        }

        String logFilePrefix = new SimpleDateFormat("HHmm").format(currentDate);
        String fileName = logFilePrefix + "_" + generateRandomString(10) + ".md";
        File file = new File(logFolder, fileName);
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(log);
        }

        // 提交并推送日志文件至评审日志仓库
        git.add().addFilepattern(logFolderName + "/" + fileName).call();
        git.commit().setMessage("docs: Add new file via GitHub Actions").call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, "")).call();
        System.out.println("Changes have been pushed to the repository.");

        return "https://github.com/burn1t/openai-code-review-log/blob/master/" + logFolderName + "/" + fileName;
    }

    public static void pushMessage(String logUrl) {
        String accessToken = WXAccessTokenUtils.getAccessToken();

        TemplateMessageDTO templateMessageDTO = new TemplateMessageDTO("wx4617d5873082286a", "28bb42cb8c3e0015b7728d6ce5510be5");
        templateMessageDTO.put("project", "openai-code-review");
        templateMessageDTO.put("review", "feat: 代码评审");
        templateMessageDTO.setUrl(logUrl);

        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken);
        sendPostRequest(url, JSON.toJSONString(templateMessageDTO));
    }

    public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(characters.charAt(random.nextInt(characters.length())));
        }
        return stringBuilder.toString();
    }

    private static void sendPostRequest(String urlString, String jsonBody) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
                String response = scanner.useDelimiter("\\A").next();
                System.out.println(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}