package com.floyd.sdk.infrastructure.openai;

import com.floyd.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import com.floyd.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;

public interface IOpenAI {

    ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO requestDTO) throws Exception;

}
