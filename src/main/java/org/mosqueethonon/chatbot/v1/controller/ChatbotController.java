package org.mosqueethonon.chatbot.v1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.chatbot.service.ChatbotIndexingService;
import org.mosqueethonon.chatbot.service.ChatbotService;
import org.mosqueethonon.chatbot.v1.dto.ChatbotFeedbackRequestDto;
import org.mosqueethonon.chatbot.v1.dto.ChatbotMessageRequestDto;
import org.mosqueethonon.chatbot.v1.dto.ChatbotMessageResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    private final ChatbotIndexingService chatbotIndexingService;

    @PostMapping("/messages")
    public ResponseEntity<ChatbotMessageResponseDto> sendMessage(@Valid @RequestBody ChatbotMessageRequestDto request) {
        ChatbotMessageResponseDto response = this.chatbotService.sendMessage(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/messages/{messageId}/feedback")
    public ResponseEntity<Void> setFeedback(@PathVariable Long messageId, @Valid @RequestBody ChatbotFeedbackRequestDto request) {
        this.chatbotService.setFeedback(messageId, request.getFeedback());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reindex")
    public ResponseEntity<Void> reindex() {
        int count = this.chatbotIndexingService.reindex();
        log.info("Réindexation chatbot déclenchée manuellement : {} chunk(s)", count);
        return ResponseEntity.ok().build();
    }

}
