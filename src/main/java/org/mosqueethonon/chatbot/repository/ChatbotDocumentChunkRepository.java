package org.mosqueethonon.chatbot.repository;

import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import org.mosqueethonon.chatbot.entity.ChatbotDocumentChunkEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Accès à moth.chatbot_document_chunk via JdbcTemplate (pas de JpaRepository standard car la
 * colonne embedding est un type pgvector natif, écrit/lu via {@link PGvector}).
 */
@Repository
@RequiredArgsConstructor
public class ChatbotDocumentChunkRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<ChatbotDocumentChunkEntity> CHUNK_ROW_MAPPER = (rs, rowNum) ->
            ChatbotDocumentChunkEntity.builder()
                    .id(rs.getLong("id"))
                    .theme(rs.getString("theme"))
                    .sourceFile(rs.getString("source_file"))
                    .sectionTitle(rs.getString("section_title"))
                    .content(rs.getString("content"))
                    .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
                    .build();

    public void deleteAll() {
        this.jdbcTemplate.update("DELETE FROM moth.chatbot_document_chunk");
    }

    public void insertAll(List<ChatbotDocumentChunkEntity> chunks) {
        if (chunks.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO moth.chatbot_document_chunk " +
                "(theme, source_file, section_title, content, embedding, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        this.jdbcTemplate.batchUpdate(sql, chunks, chunks.size(), (ps, chunk) -> {
            ps.setString(1, chunk.getTheme());
            ps.setString(2, chunk.getSourceFile());
            ps.setString(3, chunk.getSectionTitle());
            ps.setString(4, chunk.getContent());
            setEmbedding(ps, 5, chunk.getEmbedding());
            LocalDateTime createdAt = chunk.getCreatedAt() != null ? chunk.getCreatedAt() : LocalDateTime.now();
            ps.setTimestamp(6, Timestamp.valueOf(createdAt));
        });
    }

    /**
     * Recherche des k chunks les plus proches de l'embedding fourni, par distance cosinus
     * pgvector (opérateur {@code <=>}). Le score renvoyé est la similarité (1 - distance).
     * <p>
     * Note : gemini-embedding-001 ne renvoie pas de vecteurs normalisés en dessous de 3072
     * dimensions, mais la distance cosinus est invariante par l'échelle, donc aucune normalisation
     * L2 n'est nécessaire ici. Elle le deviendrait si l'opérateur passait au produit scalaire
     * ({@code <#>} / {@code vector_ip_ops}).
     */
    public List<ChatbotChunkMatch> findTopKBySimilarity(float[] queryEmbedding, int k) {
        String sql = "SELECT id, theme, source_file, section_title, content, created_at, " +
                "1 - (embedding <=> ?) AS similarity " +
                "FROM moth.chatbot_document_chunk " +
                "ORDER BY embedding <=> ? " +
                "LIMIT ?";
        PGvector vector = new PGvector(queryEmbedding);
        return this.jdbcTemplate.query(sql, ps -> {
            ps.setObject(1, vector);
            ps.setObject(2, vector);
            ps.setInt(3, k);
        }, (rs, rowNum) -> new ChatbotChunkMatch(CHUNK_ROW_MAPPER.mapRow(rs, rowNum), rs.getDouble("similarity")));
    }

    private void setEmbedding(java.sql.PreparedStatement ps, int index, float[] embedding) throws SQLException {
        if (embedding == null) {
            ps.setNull(index, Types.OTHER);
        } else {
            ps.setObject(index, new PGvector(embedding));
        }
    }

}
