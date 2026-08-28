package org.mosqueethonon.chatbot.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.chatbot.config.ChatbotProperties;
import org.mosqueethonon.chatbot.service.ChatbotIndexingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;

/**
 * Maintient l'index du chatbot aligné sur le contenu de {@code docs/functional} sans redéploiement :
 * la documentation est poussée sur le serveur par le workflow {@code update-chatbot-docs}, ce job la
 * relit et ne déclenche une réindexation que si sa signature a changé.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotIndexingJob {

    private final ChatbotIndexingService chatbotIndexingService;

    private final ChatbotProperties chatbotProperties;

    /**
     * {@code fixedDelay} n'a pas de délai initial : le premier cycle a lieu au démarrage de
     * l'application, ce qui couvre aussi un déploiement livrant une documentation modifiée.
     *
     * <p>Un échec est logué mais jamais propagé. Une exception qui remonterait au scheduler
     * annulerait définitivement les exécutions suivantes, et une API Gemini indisponible ou un quota
     * épuisé ne doit pas davantage empêcher l'application de démarrer — le reste des fonctionnalités
     * n'a rien à voir avec le chatbot, et l'endpoint {@code POST /v1/chatbot/reindex} reste
     * disponible pour rattraper.
     */
    @Scheduled(fixedDelayString = "${chatbot.indexing.check-interval}", timeUnit = TimeUnit.SECONDS)
    public void reindexIfOutdated() {
        if (!Boolean.TRUE.equals(this.chatbotProperties.getIndexing().getEnabled())) {
            log.debug("Indexation chatbot désactivée (chatbot.indexing.enabled=false)");
            return;
        }

        try {
            OptionalInt indexed = this.chatbotIndexingService.reindexIfOutdated();
            if (indexed.isPresent()) {
                log.info("Indexation chatbot : {} chunk(s) réindexé(s)", indexed.getAsInt());
            }
        } catch (Exception e) {
            log.error("Échec de l'indexation chatbot — utilisez POST /v1/chatbot/reindex pour réessayer", e);
        }
    }

}
