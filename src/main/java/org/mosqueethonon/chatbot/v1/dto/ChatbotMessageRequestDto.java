package org.mosqueethonon.chatbot.v1.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatbotMessageRequestDto {

    private Long conversationId;

    @NotBlank
    private String question;

}
