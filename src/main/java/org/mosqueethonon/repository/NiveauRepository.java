package org.mosqueethonon.repository;

import org.mosqueethonon.entity.referentiel.NiveauEntity;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NiveauRepository extends JpaRepository<NiveauEntity, Long> {

    @Query("SELECT n.niveauSuperieur FROM NiveauEntity n WHERE n.niveau = :niveau")
    NiveauInterneEnum findNiveauSuperieurByNiveau(NiveauInterneEnum niveau);

}
