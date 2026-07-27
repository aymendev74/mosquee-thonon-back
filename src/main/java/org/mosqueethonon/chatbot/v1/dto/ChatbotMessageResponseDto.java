package org.mosqueethonon.chatbot.v1.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotMessageResponseDto {

    private Long conversationId;

    private Long messageId;

    private String answer;

    private List<ChatbotSourceDto> sources;

}
