package org.mosqueethonon.chatbot.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.chatbot.config.ChatbotProperties;
import org.mosqueethonon.chatbot.entity.ChatbotConversationEntity;
import org.mosqueethonon.chatbot.entity.ChatbotDocumentChunkEntity;
import org.mosqueethonon.chatbot.entity.ChatbotMessageEntity;
import org.mosqueethonon.chatbot.enums.ChatbotFeedbackEnum;
import org.mosqueethonon.chatbot.enums.ChatbotRoleEnum;
import org.mosqueethonon.chatbot.repository.ChatbotChunkMatch;
import org.mosqueethonon.chatbot.repository.ChatbotConversationRepository;
import org.mosqueethonon.chatbot.repository.ChatbotMessageRepository;
import org.mosqueethonon.chatbot.service.ChatbotGenerationService;
import org.mosqueethonon.chatbot.service.ChatbotRetrievalService;
import org.mosqueethonon.configuration.security.context.SecurityContext;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.utilisateur.repository.UtilisateurRepository;
import org.mosqueethonon.chatbot.v1.dto.ChatbotMessageRequestDto;
import org.mosqueethonon.chatbot.v1.dto.ChatbotMessageResponseDto;
import org.mosqueethonon.chatbot.v1.dto.ChatbotSourceDto;
import org.mosqueethonon.chatbot.v1.mapper.ChatbotSourceMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Vérifie surtout le garde-fou anti-hallucination : quand le retrieval ne remonte aucun chunk
 * pertinent, le contexte transmis à la génération doit être vide (c'est l'instruction système
 * de ChatbotGenerationService qui impose alors la réponse "je ne sais pas" ; ce test s'assure
 * que l'orchestrateur ne "comble" jamais artificiellement le contexte).
 */
@ExtendWith(MockitoExtension.class)
public class TestChatbotServiceImpl {

    @Mock
    private ChatbotRetrievalService chatbotRetrievalService;

    @Mock
    private ChatbotGenerationService chatbotGenerationService;

    @Mock
    private ChatbotConversationRepository chatbotConversationRepository;

    @Mock
    private ChatbotMessageRepository chatbotMessageRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private ChatbotSourceMapper chatbotSourceMapper;

    @Mock
    private ChatbotProperties chatbotProperties;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ChatbotServiceImpl underTest;

    private void stubNewConversation(long conversationId) {
        ChatbotConversationEntity savedConversation = new ChatbotConversationEntity();
        savedConversation.setId(conversationId);
        when(this.chatbotConversationRepository.save(any())).thenReturn(savedConversation);
        when(this.chatbotMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)).thenReturn(List.of());
        when(this.chatbotMessageRepository.save(any())).thenAnswer(invocation -> {
            ChatbotMessageEntity message = invocation.getArgument(0);
            message.setId(42L);
            return message;
        });
        ChatbotProperties.Gemini gemini = new ChatbotProperties.Gemini();
        gemini.setGenerationModel("gemini-2.5-flash");
        when(this.chatbotProperties.getGemini()).thenReturn(gemini);
    }

    @Test
    public void testSendMessage_NoRelevantChunks_PassesEmptyContextToGeneration() {
        // GIVEN
        stubNewConversation(100L);
        when(this.chatbotRetrievalService.retrieve(anyString())).thenReturn(List.of());
        when(this.chatbotGenerationService.generate(anyList(), anyString(), anyString()))
                .thenReturn("Je n'ai pas cette information dans la documentation.");

        ChatbotMessageRequestDto request = new ChatbotMessageRequestDto();
        request.setQuestion("Quelle est la couleur du ciel ?");

        // WHEN
        ChatbotMessageResponseDto response = underTest.sendMessage(request);

        // THEN
        ArgumentCaptor<String> contextCaptor = ArgumentCaptor.forClass(String.class);
        verify(this.chatbotGenerationService).generate(anyList(), contextCaptor.capture(), eq("Quelle est la couleur du ciel ?"));
        assertEquals("", contextCaptor.getValue());
        assertTrue(response.getSources().isEmpty());
        assertEquals("Je n'ai pas cette information dans la documentation.", response.getAnswer());
        verify(this.chatbotSourceMapper, never()).toDto(any());
    }

    @Test
    public void testSendMessage_WithRelevantChunks_BuildsContextAndReturnsSources() {
        // GIVEN
        stubNewConversation(100L);
        ChatbotDocumentChunkEntity chunk = ChatbotDocumentChunkEntity.builder()
                .id(7L)
                .theme("tarifs")
                .sectionTitle("Notion de tarif")
                .content("## Notion de tarif\nUn tarif est une case de prix.")
                .build();
        ChatbotChunkMatch match = new ChatbotChunkMatch(chunk, 0.82);

        when(this.chatbotRetrievalService.retrieve(anyString())).thenReturn(List.of(match));
        when(this.chatbotGenerationService.generate(anyList(), anyString(), anyString()))
                .thenReturn("Un tarif est une case de prix.");
        ChatbotSourceDto sourceDto = ChatbotSourceDto.builder()
                .chunkId(7L).theme("tarifs").sectionTitle("Notion de tarif").score(0.82).build();
        when(this.chatbotSourceMapper.toDto(match)).thenReturn(sourceDto);

        ChatbotMessageRequestDto request = new ChatbotMessageRequestDto();
        request.setQuestion("Qu'est-ce qu'un tarif ?");

        // WHEN
        ChatbotMessageResponseDto response = underTest.sendMessage(request);

        // THEN
        ArgumentCaptor<String> contextCaptor = ArgumentCaptor.forClass(String.class);
        verify(this.chatbotGenerationService).generate(anyList(), contextCaptor.capture(), eq("Qu'est-ce qu'un tarif ?"));
        assertTrue(contextCaptor.getValue().contains("Un tarif est une case de prix."));
        assertEquals(1, response.getSources().size());
        assertEquals(7L, response.getSources().get(0).getChunkId());
        assertEquals(100L, response.getConversationId());
        assertEquals(42L, response.getMessageId());

        // 2 messages persistés : USER puis ASSISTANT
        verify(this.chatbotMessageRepository, times(2)).save(any());
    }

    @Test
    public void testSendMessage_ExistingConversationNotFound_ThrowsResourceNotFoundException() {
        // GIVEN
        when(this.chatbotConversationRepository.findById(999L)).thenReturn(Optional.empty());
        ChatbotMessageRequestDto request = new ChatbotMessageRequestDto();
        request.setConversationId(999L);
        request.setQuestion("Question");

        // WHEN / THEN
        assertThrows(ResourceNotFoundException.class, () -> underTest.sendMessage(request));
    }

    @Test
    public void testSetFeedback_UpdatesMessage() {
        // GIVEN
        ChatbotMessageEntity message = new ChatbotMessageEntity();
        message.setId(5L);
        message.setRole(ChatbotRoleEnum.ASSISTANT);
        when(this.chatbotMessageRepository.findById(5L)).thenReturn(Optional.of(message));
        when(this.chatbotMessageRepository.save(message)).thenReturn(message);

        // WHEN
        underTest.setFeedback(5L, ChatbotFeedbackEnum.THUMBS_UP);

        // THEN
        assertEquals(ChatbotFeedbackEnum.THUMBS_UP, message.getFeedback());
        verify(this.chatbotMessageRepository).save(message);
    }

    @Test
    public void testSetFeedback_MessageNotFound_ThrowsResourceNotFoundException() {
        // GIVEN
        when(this.chatbotMessageRepository.findById(404L)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThrows(ResourceNotFoundException.class, () -> underTest.setFeedback(404L, ChatbotFeedbackEnum.THUMBS_DOWN));
    }

}
