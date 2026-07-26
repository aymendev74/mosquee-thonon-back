package org.mosqueethonon.chatbot.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.chatbot.config.ChatbotProperties;
import org.mosqueethonon.chatbot.entity.ChatbotConversationEntity;
import org.mosqueethonon.chatbot.entity.ChatbotMessageEntity;
import org.mosqueethonon.chatbot.enums.ChatbotFeedbackEnum;
import org.mosqueethonon.chatbot.enums.ChatbotRoleEnum;
import org.mosqueethonon.chatbot.repository.ChatbotChunkMatch;
import org.mosqueethonon.chatbot.repository.ChatbotConversationRepository;
import org.mosqueethonon.chatbot.repository.ChatbotMessageRepository;
import org.mosqueethonon.chatbot.service.ChatbotGenerationService;
import org.mosqueethonon.chatbot.service.ChatbotRetrievalService;
import org.mosqueethonon.chatbot.service.ChatbotService;
import org.mosqueethonon.chatbot.service.ChatbotTurn;
import org.mosqueethonon.common.security.context.SecurityContext;
import org.mosqueethonon.utilisateur.entity.UtilisateurEntity;
import org.mosqueethonon.common.exception.ResourceNotFoundException;
import org.mosqueethonon.utilisateur.repository.UtilisateurRepository;
import org.mosqueethonon.chatbot.v1.dto.ChatbotMessageRequestDto;
import org.mosqueethonon.chatbot.v1.dto.ChatbotMessageResponseDto;
import org.mosqueethonon.chatbot.v1.dto.ChatbotSourceDto;
import org.mosqueethonon.chatbot.v1.mapper.ChatbotSourceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotServiceImpl implements ChatbotService {

    private final ChatbotRetrievalService chatbotRetrievalService;

    private final ChatbotGenerationService chatbotGenerationService;

    private final ChatbotConversationRepository chatbotConversationRepository;

    private final ChatbotMessageRepository chatbotMessageRepository;

    private final UtilisateurRepository utilisateurRepository;

    private final SecurityContext securityContext;

    private final ChatbotSourceMapper chatbotSourceMapper;

    private final ChatbotProperties chatbotProperties;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ChatbotMessageResponseDto sendMessage(ChatbotMessageRequestDto request) {
        ChatbotConversationEntity conversation = resolveConversation(request.getConversationId());

        List<ChatbotTurn> history = this.chatbotMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId()).stream()
                .map(message -> new ChatbotTurn(message.getRole(), message.getContent()))
                .toList();

        List<ChatbotChunkMatch> matches = this.chatbotRetrievalService.retrieve(request.getQuestion());
        String context = buildContext(matches);

        // Garde-fou anti-hallucination : si aucun chunk pertinent, le contexte est vide et
        // l'instruction système de ChatbotGenerationService impose de répondre qu'on ne sait pas.
        String answer = this.chatbotGenerationService.generate(history, context, request.getQuestion());

        this.chatbotMessageRepository.save(buildMessage(conversation.getId(), ChatbotRoleEnum.USER, request.getQuestion(), null, null));

        List<ChatbotSourceDto> sources = matches.stream().map(this.chatbotSourceMapper::toDto).toList();
        ChatbotMessageEntity assistantMessage = this.chatbotMessageRepository.save(buildMessage(
                conversation.getId(),
                ChatbotRoleEnum.ASSISTANT,
                answer,
                toJson(sources),
                this.chatbotProperties.getGemini().getGenerationModel()
        ));

        return ChatbotMessageResponseDto.builder()
                .conversationId(conversation.getId())
                .messageId(assistantMessage.getId())
                .answer(answer)
                .sources(sources)
                .build();
    }

    @Override
    @Transactional
    public void setFeedback(Long messageId, ChatbotFeedbackEnum feedback) {
        ChatbotMessageEntity message = this.chatbotMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message chatbot introuvable : id=" + messageId));
        message.setFeedback(feedback);
        this.chatbotMessageRepository.save(message);
    }

    private ChatbotConversationEntity resolveConversation(Long conversationId) {
        if (conversationId != null) {
            return this.chatbotConversationRepository.findById(conversationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation chatbot introuvable : id=" + conversationId));
        }
        ChatbotConversationEntity conversation = new ChatbotConversationEntity();
        conversation.setUtilisateurId(resolveCurrentUtilisateurId());
        conversation.setCreatedAt(LocalDateTime.now());
        return this.chatbotConversationRepository.save(conversation);
    }

    private Long resolveCurrentUtilisateurId() {
        String username = this.securityContext.getUser();
        if (username == null) {
            return null;
        }
        return this.utilisateurRepository.findByUsername(username).map(UtilisateurEntity::getId).orElse(null);
    }

    private ChatbotMessageEntity buildMessage(Long conversationId, ChatbotRoleEnum role, String content, String sources, String model) {
        ChatbotMessageEntity message = new ChatbotMessageEntity();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setSources(sources);
        message.setModel(model);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }

    private String buildContext(List<ChatbotChunkMatch> matches) {
        if (matches.isEmpty()) {
            return "";
        }
        return matches.stream()
                .map(match -> "[" + match.chunk().getTheme() + " / " + match.chunk().getSectionTitle() + "]\n" + match.chunk().getContent())
                .reduce((a, b) -> a + "\n\n---\n\n" + b)
                .orElse("");
    }

    private String toJson(List<ChatbotSourceDto> sources) {
        if (sources.isEmpty()) {
            return null;
        }
        try {
            return this.objectMapper.writeValueAsString(sources);
        } catch (JsonProcessingException e) {
            log.error("Impossible de sérialiser les sources du chatbot en JSON", e);
            return null;
        }
    }

}
