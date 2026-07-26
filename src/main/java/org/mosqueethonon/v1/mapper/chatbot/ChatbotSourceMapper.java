package org.mosqueethonon.v1.mapper.chatbot;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.chatbot.repository.ChatbotChunkMatch;
import org.mosqueethonon.v1.dto.chatbot.ChatbotSourceDto;

@Mapper(componentModel = "spring")
public interface ChatbotSourceMapper {

    @Mapping(target = "chunkId", source = "chunk.id")
    @Mapping(target = "theme", source = "chunk.theme")
    @Mapping(target = "sectionTitle", source = "chunk.sectionTitle")
    @Mapping(target = "score", source = "score")
    ChatbotSourceDto toDto(ChatbotChunkMatch match);

}
