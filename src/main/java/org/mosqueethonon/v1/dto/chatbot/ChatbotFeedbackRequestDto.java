package org.mosqueethonon.v1.dto.chatbot;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.mosqueethonon.chatbot.enums.ChatbotFeedbackEnum;

@Data
public class ChatbotFeedbackRequestDto {

    @NotNull
    private ChatbotFeedbackEnum feedback;

}
