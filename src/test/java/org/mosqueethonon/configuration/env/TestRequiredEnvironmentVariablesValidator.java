package org.mosqueethonon.configuration.env;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verrouille le garde-fou qui interdit à l'application de démarrer avec une variable
 * d'environnement absente. Sans lui, le binder des @ConfigurationProperties injecte le placeholder
 * en clair et la configuration paraît valide.
 */
public class TestRequiredEnvironmentVariablesValidator {

    private StandardEnvironment environmentWith(Map<String, Object> configFile, Map<String, Object> environmentVariables) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        if (environmentVariables != null) {
            environment.getPropertySources().addFirst(new MapPropertySource("variables", environmentVariables));
        }
        environment.getPropertySources()
                .addLast(new OriginTrackedMapPropertySource("application.yml", configFile));
        return environment;
    }

    @Test
    void testVariableRenseigneeNestPasSignalee() {
        StandardEnvironment environment = environmentWith(
                Map.of("chatbot.gemini.api-key", "${GEMINI_API_KEY}"),
                Map.of("GEMINI_API_KEY", "une-vraie-cle"));

        assertTrue(RequiredEnvironmentVariablesValidator.findUnresolvedPlaceholders(environment).isEmpty());
    }

    @Test
    void testVariableAbsenteEstSignaleeAvecSonPlaceholder() {
        StandardEnvironment environment = environmentWith(
                Map.of("chatbot.gemini.api-key", "${GEMINI_API_KEY}"), null);

        Map<String, String> unresolved =
                RequiredEnvironmentVariablesValidator.findUnresolvedPlaceholders(environment);

        assertEquals(1, unresolved.size());
        assertEquals("${GEMINI_API_KEY}", unresolved.get("chatbot.gemini.api-key"));
    }

    /**
     * Documente la raison d'être du garde-fou. La résolution tolérante — celle qu'emploie le binder
     * des {@code @ConfigurationProperties} — rend le texte brut au lieu d'échouer, et {@code @NotBlank}
     * ne peut alors plus distinguer ce littéral d'une vraie valeur. Le contrôle doit donc s'appuyer
     * sur la résolution stricte.
     */
    @Test
    void testResolutionToleranteRendLeTexteBrutLaOuLaResolutionStricteEchoue() {
        StandardEnvironment environment = environmentWith(
                Map.of("chatbot.gemini.api-key", "${GEMINI_API_KEY}"), null);

        assertEquals("${GEMINI_API_KEY}", environment.resolvePlaceholders("${GEMINI_API_KEY}"),
                "la résolution tolérante conserve le placeholder en clair");
        assertFalse(RequiredEnvironmentVariablesValidator.findUnresolvedPlaceholders(environment).isEmpty(),
                "le garde-fou, lui, doit le refuser");
    }

    @Test
    void testValeurAvecDefautNestPasSignalee() {
        StandardEnvironment environment = environmentWith(
                Map.of("app.documents.base-path", "${DOCUMENTS_BASE_PATH:/tmp/documents}"), null);

        assertTrue(RequiredEnvironmentVariablesValidator.findUnresolvedPlaceholders(environment).isEmpty());
    }

    @Test
    void testValeurSansPlaceholderEstIgnoree() {
        StandardEnvironment environment = environmentWith(Map.of("server.port", "8080"), null);

        assertTrue(RequiredEnvironmentVariablesValidator.findUnresolvedPlaceholders(environment).isEmpty());
    }

    /**
     * Une source plus prioritaire (application-test.yml, propriété système) remplace la valeur du
     * fichier principal : le placeholder de ce dernier n'est jamais utilisé, donc jamais signalé.
     */
    @Test
    void testValeurSurchargeeParUneSourcePrioritaireNestPasSignalee() {
        StandardEnvironment environment = environmentWith(
                Map.of("spring.datasource.url", "${DB_URL}"),
                Map.of("spring.datasource.url", "jdbc:h2:mem:testdb"));

        assertTrue(RequiredEnvironmentVariablesValidator.findUnresolvedPlaceholders(environment).isEmpty());
    }

    @Test
    void testPlusieursVariablesAbsentesSontToutesSignalees() {
        StandardEnvironment environment = environmentWith(
                Map.of("app.documents.base-path", "${DOCUMENTS_BASE_PATH}",
                        "chatbot.gemini.api-key", "${GEMINI_API_KEY}"), null);

        Map<String, String> unresolved =
                RequiredEnvironmentVariablesValidator.findUnresolvedPlaceholders(environment);

        assertEquals(2, unresolved.size());
        assertTrue(unresolved.containsKey("app.documents.base-path"));
        assertTrue(unresolved.containsKey("chatbot.gemini.api-key"));
    }

}
