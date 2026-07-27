package org.mosqueethonon.chatbot.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Accès à moth.chatbot_index_state, table à ligne unique (id = 1) portant la signature de l'index
 * chatbot. Même choix que {@link ChatbotDocumentChunkRepository} : JdbcTemplate plutôt que JPA, pour
 * rester cohérent au sein du module.
 */
@Repository
@RequiredArgsConstructor
public class ChatbotIndexStateRepository {

    private final Clock clock;

    private static final long SINGLE_ROW_ID = 1L;

    private final JdbcTemplate jdbcTemplate;

    /**
     * Lit la signature courante en verrouillant la ligne jusqu'à la fin de la transaction en cours.
     * En environnement multi-instances, la seconde instance à démarrer attend ici, puis relit une
     * signature à jour et n'a plus rien à réindexer. À appeler dans une transaction.
     */
    public String findSignatureForUpdate() {
        // Auto-réparation : la ligne est semée par le changeset 065, mais si elle a disparu (base
        // restaurée depuis un instantané antérieur, suppression manuelle), on la recrée au lieu
        // d'échouer — sinon plus aucune réindexation ne serait possible, ni au démarrage ni via
        // l'endpoint admin. ON CONFLICT rend l'opération sûre si deux instances démarrent ensemble.
        this.jdbcTemplate.update("INSERT INTO moth.chatbot_index_state (id, signature, chunk_count) "
                + "VALUES (?, '', 0) ON CONFLICT (id) DO NOTHING", SINGLE_ROW_ID);

        return this.jdbcTemplate.queryForObject(
                "SELECT signature FROM moth.chatbot_index_state WHERE id = ? FOR UPDATE",
                String.class, SINGLE_ROW_ID);
    }

    public void updateSignature(String signature, int chunkCount) {
        this.jdbcTemplate.update(
                "UPDATE moth.chatbot_index_state SET signature = ?, chunk_count = ?, indexed_at = ? WHERE id = ?",
                signature, chunkCount, LocalDateTime.now(clock), SINGLE_ROW_ID);
    }

}
