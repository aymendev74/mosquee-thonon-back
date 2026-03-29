package org.mosqueethonon.repository;

import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PeriodeRepository extends JpaRepository<PeriodeEntity, Long> {

    List<PeriodeEntity> findByApplicationAndIdNot(String application, Long id);

    List<PeriodeEntity> findByApplication(String application);

    PeriodeEntity findFirstByApplicationOrderByDateDebutDesc(String application);

    Optional<PeriodeEntity> findByApplicationAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(String application, LocalDate dateDebut, LocalDate dateFin);

    @Query(value = "SELECT * FROM moth.periode WHERE idperi = :id FOR UPDATE", nativeQuery = true)
    Optional<PeriodeEntity> lockById(@Param("id") Long id);

}
