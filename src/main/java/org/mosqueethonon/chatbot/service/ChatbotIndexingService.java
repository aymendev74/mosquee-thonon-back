package org.mosqueethonon.chatbot.service;

import java.util.OptionalInt;

public interface ChatbotIndexingService {

    /**
     * Relit tous les fichiers markdown de {@code chatbot.docs-path} (hors README.md), les
     * découpe par section (##/###), calcule les embeddings, puis remplace intégralement le
     * contenu de moth.chatbot_document_chunk (purge + réinsertion).
     * <p>
     * Réindexe inconditionnellement, même si rien n'a changé.
     *
     * @return le nombre de chunks indexés
     */
    int reindex();

    /**
     * Ne réindexe que si la signature de l'index a changé, c'est-à-dire si le contenu des chunks,
     * le modèle d'embedding ou la dimension diffèrent de ce qui est actuellement stocké. Appelée au
     * démarrage de l'application pour éviter de consommer du quota Gemini sans raison.
     *
     * @return le nombre de chunks indexés, ou {@link OptionalInt#empty()} si l'index était déjà à jour
     */
    OptionalInt reindexIfOutdated();

}
