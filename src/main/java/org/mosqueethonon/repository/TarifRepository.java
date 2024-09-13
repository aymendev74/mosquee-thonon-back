package org.mosqueethonon.repository;

import org.mosqueethonon.entity.TarifEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarifRepository extends JpaRepository<TarifEntity, Long>, JpaSpecificationExecutor<TarifEntity> {

    public List<TarifEntity> findByPeriodeId(Long idPeriode);

}
