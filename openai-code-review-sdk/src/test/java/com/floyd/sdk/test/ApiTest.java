package com.floyd.sdk.test;

import com.alibaba.fastjson2.JSON;
import com.floyd.sdk.types.utils.BearerTokenUtils;
import com.floyd.sdk.types.utils.WXAccessTokenUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;


public class ApiTest {

    public final static String API_KEY_SECRET = "d37c4f500b504addbb5e265fc933daa0.czrCITvIzMuM9NNI";

    public final static String URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    public static void main(String[] args) {
        String token = BearerTokenUtils.getToken(API_KEY_SECRET);
        System.out.println(token);
    }

    @Test
    public void test_http() throws IOException {
        String token = BearerTokenUtils.getToken(API_KEY_SECRET);

        URL url = new URL(URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String code = "1 + 1";
        String jsonStr = "{"
                + "\"model\":\"glm-4-flash\","
                + "\"messages\": ["
                + "    {"
                + "        \"role\": \"user\","
                + "        \"content\": \"你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码为: " + code + "\""
                + "    }"
                + "]"
                + "}";

        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] bytes = jsonStr.getBytes(StandardCharsets.UTF_8);
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
        System.out.println("content: " + content);
    }

    @Test
    public void send_message() {
        String accessToken = WXAccessTokenUtils.getAccessToken();

        Message message = new Message();
        message.put("project", "openai-code-review");
        message.put("review", "feat: 代码评审");

        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken);

        sendPostRequest(url, JSON.toJSONString(message));
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

    public class Message {

        String touser = "os9OmvvuCI4rn691AQMT7wNVP6IA";

        String template_id = "fIF8KEE0MOrlv6CuAaVtzKMn4T_OTr3TJeOu8lWXGhc";

        // 日志 url
        String url = "https://github.com/burn1t/openai-code-review-log/blob/main/2026-03-06/0728_7VJhhUYpM6.md";

        HashMap<String, HashMap<String, String>> data = new HashMap<>();

        public void put(String key, String value) {
            data.put(key, new HashMap<String, String>() {
                {
                    put("value", value);
                }
            });
        }

        public String getTouser() {
            return touser;
        }

        public void setTouser(String touser) {
            this.touser = touser;
        }

        public String getTemplate_id() {
            return template_id;
        }

        public void setTemplate_id(String template_id) {
            this.template_id = template_id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public HashMap<String, HashMap<String, String>> getData() {
            return data;
        }

        public void setData(HashMap<String, HashMap<String, String>> data) {
            this.data = data;
        }
    }

}
