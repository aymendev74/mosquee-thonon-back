package org.mosqueethonon.chatbot.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "chatbot", ignoreUnknownFields = false)
@Validated
@Data
public class ChatbotProperties {

    @Valid
    @NotNull
    private Gemini gemini;

    @Valid
    @NotNull
    private Retrieval retrieval;

    @Valid
    @NotNull
    private Indexing indexing;

    @NotBlank
    private String docsPath;

    @Data
    public static final class Gemini {
        /** Exclu du toString() généré : la clé ne doit jamais atterrir dans un log. */
        @ToString.Exclude
        @NotBlank
        private String apiKey;
        /**
         * Identifiant nu du modèle, sans le préfixe {@code models/} : celui-ci fait partie du
         * template d'URI côté service. Un préfixe ici serait ré-encodé en {@code %2F} par
         * {@code DefaultUriBuilderFactory} et produirait un 404.
         */
        @NotBlank
        @Pattern(regexp = "[A-Za-z0-9._-]+", message = "doit être un id de modèle nu, sans préfixe 'models/'")
        private String embeddingModel;
        /**
         * Dimension demandée à l'API pour les embeddings. Doit correspondre exactement à la colonne
         * {@code moth.chatbot_document_chunk.embedding vector(768)} (changelog 063).
         */
        @NotNull
        @Min(1)
        private Integer embeddingDimension;
        @NotBlank
        @Pattern(regexp = "[A-Za-z0-9._-]+", message = "doit être un id de modèle nu, sans préfixe 'models/'")
        private String generationModel;
        /**
         * Température de génération. Une valeur basse est volontaire : le chatbot est factuel et doit
         * respecter à la lettre le garde-fou et le format de sortie imposés par l'instruction système.
         */
        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("2.0")
        private Double temperature;
        @NotBlank
        private String baseUrl;
    }

    @Data
    public static final class Retrieval {
        @NotNull
        private Integer topK;
        @NotNull
        private Double minScore;
    }

    @Data
    public static final class Indexing {
        /**
         * Réindexation conditionnelle au démarrage. Doit rester à false dans les tests : le contexte
         * Spring complet déclenche ApplicationReadyEvent, ce qui appellerait réellement l'API Gemini.
         */
        @NotNull
        private Boolean onStartup;
    }

}
