package org.mosqueethonon.scheduled;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Slf4j
public class DocumentRequestsJob {

    private final DocumentRequestJobService documentRequestJobService;

    /**
     * Traite toutes les demandes de génération de documents en statut PENDING.
     * En environnement multi-instances, chaque instance boucle sur les enregistrements disponibles :
     * SELECT FOR UPDATE SKIP LOCKED garantit qu'un enregistrement ne peut être traité que par
     * une seule instance à la fois, sans nécessiter de statut intermédiaire.
     * La boucle s'arrête dès qu'il n'y a plus d'enregistrement disponible (file vide ou tous lockés
     * par d'autres instances).
     */
    @Scheduled(fixedDelayString = "${scheduled.document-generation}", timeUnit = TimeUnit.MINUTES)
    public void processPendingDocumentRequests() {
        log.debug("Démarrage du job de traitement des demandes de génération de documents");
        int processed = 0;
        try {
            while (documentRequestJobService.processNextPendingRequest()) {
                processed++;
            }
        } catch (Exception e) {
            // En cas d'erreur critique non récupérée (ex: échec du save interne du processeur),
            // la transaction a été annulée et le record reste PENDING pour le prochain cycle.
            log.error("Arrêt anticipé du job après une erreur critique — {} demande(s) traitée(s) avant l'incident", processed, e);
        }
        if (processed > 0) {
            log.info("{} demande(s) de génération de documents traitée(s)", processed);
        } else {
            log.debug("Aucune demande de génération de documents à traiter");
        }
    }

}
