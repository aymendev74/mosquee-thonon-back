package org.mosqueethonon.repository;

import org.mosqueethonon.entity.inscription.ReinscriptionPrioritaireEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReinscriptionPrioritaireRepository extends JpaRepository<ReinscriptionPrioritaireEntity, Long> {

    ReinscriptionPrioritaireEntity findByNomIgnoreCaseAndPrenomIgnoreCase(String nom, String prenom);

    @Query(value = "select * from moth.reinscriptionprioritaire " +
            "where dmetaphone(:nom) = dmetaphone(txreprnom) " +
            "and dmetaphone(:prenom) = dmetaphone(txreprprenom) ", nativeQuery = true)
    ReinscriptionPrioritaireEntity findByNomAndPrenomPhonetique(String nom, String prenom);

}
