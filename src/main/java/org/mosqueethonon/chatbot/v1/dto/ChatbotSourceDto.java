package org.mosqueethonon.chatbot.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotSourceDto {

    private Long chunkId;

    private String theme;

    private String sectionTitle;

    private Double score;

}
