package org.mosqueethonon.chatbot.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.chatbot.repository.ChatbotChunkMatch;
import org.mosqueethonon.chatbot.v1.dto.ChatbotSourceDto;

@Mapper(componentModel = "spring")
public interface ChatbotSourceMapper {

    @Mapping(target = "chunkId", source = "chunk.id")
    @Mapping(target = "theme", source = "chunk.theme")
    @Mapping(target = "sectionTitle", source = "chunk.sectionTitle")
    @Mapping(target = "score", source = "score")
    ChatbotSourceDto toDto(ChatbotChunkMatch match);

}
