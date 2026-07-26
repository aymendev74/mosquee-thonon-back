package org.mosqueethonon.v1.dto.chatbot;

import lombok.Builder;
import lombok.Data;

/**
 * Corps renvoyé avec un HTTP 429 lorsque le quota de l'API d'IA est dépassé. Volontairement
 * structuré et sans message rédigé : la formulation destinée à l'utilisateur appartient au front,
 * qui connaît la langue et le contexte d'affichage.
 */
@Data
@Builder
public class ChatbotQuotaErrorDto {

    /** Toujours {@code QUOTA_EXCEEDED} : permet au front de distinguer ce cas d'un autre 429. */
    private String reason;

    /**
     * {@code MINUTE}, {@code DAY} ou {@code UNKNOWN}. Best-effort : vaut {@code UNKNOWN} dès que le
     * corps renvoyé par Google ne permet pas de conclure, cas dans lequel le front doit se contenter
     * d'un message neutre du type « réessayez plus tard ».
     */
    private String quotaScope;

    /** Délai suggéré en secondes, ou {@code null} s'il n'a pas pu être déterminé. */
    private Integer retryAfterSeconds;

}
