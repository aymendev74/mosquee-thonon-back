package org.mosqueethonon.repository;

import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PeriodeRepository extends JpaRepository<PeriodeEntity, Long> {

    List<PeriodeEntity> findByApplicationAndIdNot(String application, Long id);

    List<PeriodeEntity> findByApplication(String application);

    @Query(value = "select p from PeriodeEntity p "
            + "where p.dateDebut <= :atDate and p.dateFin >= :atDate " +
            "and p.application = 'COURS'")
    PeriodeEntity findPeriodeCoursAtDate(LocalDate atDate);

}
