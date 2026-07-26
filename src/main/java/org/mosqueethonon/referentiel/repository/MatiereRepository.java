package org.mosqueethonon.referentiel.repository;

import org.mosqueethonon.referentiel.entity.MatiereEntity;
import org.mosqueethonon.referentiel.enums.MatiereEnum;
import org.mosqueethonon.referentiel.enums.TypeMatiereEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatiereRepository extends JpaRepository<MatiereEntity, Long> {

    Optional<MatiereEntity> findByCode(MatiereEnum code);

    List<MatiereEntity> findByType(TypeMatiereEnum type);

}
