package org.mosqueethonon.chatbot.repository;

import org.mosqueethonon.chatbot.entity.ChatbotConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatbotConversationRepository extends JpaRepository<ChatbotConversationEntity, Long> {
}
