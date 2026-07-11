package org.mosqueethonon.repository;

import org.mosqueethonon.entity.document.DocumentEntity;
import org.mosqueethonon.enums.DocumentMetadataKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {

    Optional<DocumentEntity> findByChemin(String chemin);

    List<DocumentEntity> findByIdUtilisateur(Long idUtilisateur);

    @Query("SELECT d FROM DocumentEntity d JOIN d.metadonnees m WHERE m.cle = :cle AND m.valeur = :valeur")
    Optional<DocumentEntity> findByMetadataKeyAndValue(@Param("cle") DocumentMetadataKey cle, @Param("valeur") String valeur);

}
