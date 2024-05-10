package org.mosqueethonon.repository;

import org.mosqueethonon.entity.ReinscriptionPrioritaireEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ReinscriptionPrioritaireRepository extends JpaRepository<ReinscriptionPrioritaireEntity, Long> {

    ReinscriptionPrioritaireEntity findByNomIgnoreCaseAndPrenomIgnoreCase(String nom, String prenom);

}
