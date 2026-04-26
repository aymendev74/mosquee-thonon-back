package org.mosqueethonon.repository;

import org.mosqueethonon.entity.mail.MailRequestDocumentRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailRequestDocumentRequestRepository extends JpaRepository<MailRequestDocumentRequestEntity, Long> {

    /**
     * Trouve tous les IDs de MailRequest (statut NOT_READY) liés au document donné,
     * pour lesquels il n'existe plus aucun document associé en statut autre que COMPLETED.
     * Utilise une requête SQL native pour fiabilité maximale.
     */
    @Query(value = """
            SELECT DISTINCT mdr.idmare
            FROM moth.mail_request_document_request mdr
            INNER JOIN moth.mail_request m ON m.idmare = mdr.idmare
            WHERE mdr.iddore = :documentRequestId
              AND m.cdmarestatut = :notReadyStatut
              AND NOT EXISTS (
                SELECT 1
                FROM moth.mail_request_document_request mdr2
                INNER JOIN moth.document_request d ON d.iddore = mdr2.iddore
                WHERE mdr2.idmare = mdr.idmare
                  AND d.cddorestatut <> :completedStatut
              )
            """, nativeQuery = true)
    List<Long> findReadyMailRequestIds(
            @Param("documentRequestId") Long documentRequestId,
            @Param("notReadyStatut") String notReadyStatut,
            @Param("completedStatut") String completedStatut
    );

}
