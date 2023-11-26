package org.mosqueethonon.repository;

import org.mosqueethonon.entity.InscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InscriptionRepository extends JpaRepository<InscriptionEntity, Long>, JpaSpecificationExecutor<InscriptionEntity> {

    @Query(value = "select count(*) from moth.inscription i "
            + "inner join moth.eleve e on e.idinsc = i.idinsc "
            + "inner join moth.tarif t on t.idtari = e.idtari "
            + "inner join moth.periode p on p.idperi = t.idperi "
            + "where p.idperi = :idPeriode", nativeQuery = true)
    Integer getNbElevesInscritsByIdPeriode(Long idPeriode);
}
