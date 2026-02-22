package org.mosqueethonon.repository;

import org.mosqueethonon.entity.inscription.InscriptionAdulteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InscriptionAdulteRepository extends JpaRepository<InscriptionAdulteEntity, Long> {

    @Query("SELECT DISTINCT i FROM InscriptionAdulteEntity i " +
            "JOIN FETCH i.responsableLegal " +
            "LEFT JOIN FETCH i.eleves " +
            "WHERE i.utilisateur.id = :utilisateurId")
    List<InscriptionAdulteEntity> findByUtilisateurIdWithEleves(@Param("utilisateurId") Long utilisateurId);

    @Query("SELECT DISTINCT i FROM InscriptionAdulteEntity i " +
            "LEFT JOIN FETCH i.matieres " +
            "WHERE i IN :inscriptions")
    List<InscriptionAdulteEntity> fetchMatieres(@Param("inscriptions") List<InscriptionAdulteEntity> inscriptions);

    @Query("SELECT i FROM InscriptionAdulteEntity i " +
            "JOIN FETCH i.responsableLegal " +
            "WHERE i.utilisateur.id = :utilisateurId")
    List<InscriptionAdulteEntity> findByUtilisateurId(@Param("utilisateurId") Long utilisateurId);
}
