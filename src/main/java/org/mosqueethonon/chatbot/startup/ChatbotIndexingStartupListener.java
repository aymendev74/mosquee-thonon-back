package org.mosqueethonon.chatbot.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.chatbot.config.ChatbotProperties;
import org.mosqueethonon.chatbot.service.ChatbotIndexingService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.OptionalInt;

/**
 * Déclenche une réindexation du chatbot au démarrage, mais uniquement si la documentation, le modèle
 * d'embedding ou la dimension ont changé depuis la dernière indexation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatbotIndexingStartupListener {

    private final ChatbotIndexingService chatbotIndexingService;

    private final ChatbotProperties chatbotProperties;

    /**
     * Un échec est logué mais jamais propagé : une API Gemini indisponible ou un quota épuisé ne doit
     * pas empêcher l'application de démarrer — le reste des fonctionnalités n'a rien à voir avec le
     * chatbot, et l'endpoint de réindexation manuelle reste disponible pour rattraper.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void reindexIfOutdated() {
        if (!Boolean.TRUE.equals(this.chatbotProperties.getIndexing().getOnStartup())) {
            log.info("Indexation chatbot au démarrage désactivée (chatbot.indexing.on-startup=false)");
            return;
        }

        try {
            OptionalInt indexed = this.chatbotIndexingService.reindexIfOutdated();
            if (indexed.isPresent()) {
                log.info("Indexation chatbot au démarrage : {} chunk(s) réindexé(s)", indexed.getAsInt());
            }
        } catch (Exception e) {
            log.error("Échec de l'indexation chatbot au démarrage — l'application démarre malgré tout, "
                    + "utilisez POST /v1/chatbot/reindex pour réessayer", e);
        }
    }

}
