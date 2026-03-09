package com.floyd.sdk.infrastructure.openai.impl;

import com.alibaba.fastjson2.JSON;
import com.floyd.sdk.infrastructure.openai.IOpenAI;
import com.floyd.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import com.floyd.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;
import com.floyd.sdk.types.utils.BearerTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ChatGLM implements IOpenAI {

    private final static Logger logger = LoggerFactory.getLogger(ChatGLM.class);

    private final String apiHost;

    private final String apiKeySecret;

    public ChatGLM(String apiHost, String apiKeySecret) {
        this.apiHost = apiHost;
        this.apiKeySecret = apiKeySecret;
    }

    @Override
    public ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO requestDTO) throws Exception {
        String token = BearerTokenUtils.getToken(apiKeySecret);

        URL url = new URL(apiHost);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] bytes = JSON.toJSONString(requestDTO).getBytes(StandardCharsets.UTF_8);
            outputStream.write(bytes, 0, bytes.length);
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }

        connection.disconnect();
        logger.info("ChatGLM code review done!");

        return JSON.parseObject(content.toString(), ChatCompletionSyncResponseDTO.class);
    }
}
