package com.floyd.sdk.domain.service.impl;

import com.floyd.sdk.domain.model.Model;
import com.floyd.sdk.domain.service.AbstractOpenAiCodeReviewService;
import com.floyd.sdk.infrastructure.git.GitCommand;
import com.floyd.sdk.infrastructure.openai.IOpenAI;
import com.floyd.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import com.floyd.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;
import com.floyd.sdk.infrastructure.weixin.Weixin;
import com.floyd.sdk.infrastructure.weixin.dto.TemplateMessageDTO;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class OpenAiCodeReviewService extends AbstractOpenAiCodeReviewService {

    public OpenAiCodeReviewService(GitCommand gitCommand, IOpenAI openAI, Weixin weixin) {
        super(gitCommand, openAI, weixin);
    }

    @Override
    protected String getDiffCode() throws Exception {
        return gitCommand.diff();
    }

    @Override
    protected String codeReview(String diffCode) throws Exception {
        ChatCompletionRequestDTO request = new ChatCompletionRequestDTO();
        request.setModel(Model.GLM_4_FLASH.getCode());
        request.setMessages(Arrays.asList(
                new ChatCompletionRequestDTO.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言，请您根据git diff记录，对代码做出评审。代码为"),
                new ChatCompletionRequestDTO.Prompt("user", diffCode)
        ));

        ChatCompletionSyncResponseDTO responseDTO = openAI.completions(request);
        ChatCompletionSyncResponseDTO.Message message = responseDTO.getChoices().get(0).getMessage();
        return message.getContent();
    }

    @Override
    protected String recordCodeReview(String recommend) throws Exception {
        return gitCommand.commitAndPush(recommend);
    }

    @Override
    protected void pushMessage(String logUrl) throws IOException {
        HashMap<String, HashMap<String, String>> data = new HashMap<>();
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.REPO_NAME, gitCommand.getProject());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.BRANCH_NAME, gitCommand.getBranch());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_AUTHOR, gitCommand.getAuthor());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_MESSAGE, gitCommand.getMessage());
        weixin.sendTemplateMessage(logUrl, data);
    }
}
