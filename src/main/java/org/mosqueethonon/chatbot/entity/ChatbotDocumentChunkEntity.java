package org.mosqueethonon.chatbot.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Représente une ligne de moth.chatbot_document_chunk.
 * <p>
 * Cette table est gérée en dehors de JPA/Hibernate (via {@code ChatbotDocumentChunkRepository}
 * basé sur JdbcTemplate) car la colonne {@code embedding} est du type pgvector natif
 * {@code vector(768)}, écrite/lue directement via {@link com.pgvector.PGvector}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotDocumentChunkEntity {

    private Long id;

    private String theme;

    private String sourceFile;

    private String sectionTitle;

    private String content;

    private float[] embedding;

    private LocalDateTime createdAt;

}
