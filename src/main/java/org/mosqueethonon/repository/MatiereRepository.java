package org.mosqueethonon.repository;

import org.mosqueethonon.entity.referentiel.MatiereEntity;
import org.mosqueethonon.enums.MatiereEnum;
import org.mosqueethonon.enums.TypeMatiereEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatiereRepository extends JpaRepository<MatiereEntity, Long> {

    Optional<MatiereEntity> findByCode(MatiereEnum code);

}
