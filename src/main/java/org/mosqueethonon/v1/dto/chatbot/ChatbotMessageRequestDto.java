package org.mosqueethonon.v1.dto.chatbot;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatbotMessageRequestDto {

    private Long conversationId;

    @NotBlank
    private String question;

}
