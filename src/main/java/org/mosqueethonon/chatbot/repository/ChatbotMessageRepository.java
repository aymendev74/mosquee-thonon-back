package org.mosqueethonon.chatbot.repository;

import org.mosqueethonon.chatbot.entity.ChatbotMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessageEntity, Long> {

    List<ChatbotMessageEntity> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

}
