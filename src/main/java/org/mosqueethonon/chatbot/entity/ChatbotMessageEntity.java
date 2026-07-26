package org.mosqueethonon.chatbot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.mosqueethonon.chatbot.enums.ChatbotFeedbackEnum;
import org.mosqueethonon.chatbot.enums.ChatbotRoleEnum;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_message", schema = "moth")
@Getter
@Setter
public class ChatbotMessageEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private ChatbotRoleEnum role;

    @Column(name = "content", nullable = false)
    private String content;

    /**
     * Liste des sources (chunks retenus + scores) sérialisée en JSON, uniquement renseignée
     * pour les messages ASSISTANT. Sérialisation manuelle (pas de mapping JSON Hibernate) pour
     * rester simple et testable.
     */
    @Column(name = "sources")
    private String sources;

    @Column(name = "model", length = 100)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback", length = 20)
    private ChatbotFeedbackEnum feedback;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
