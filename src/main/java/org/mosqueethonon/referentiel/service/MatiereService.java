package org.mosqueethonon.referentiel.service;

import org.mosqueethonon.referentiel.entity.MatiereEntity;
import org.mosqueethonon.referentiel.enums.MatiereEnum;
import org.mosqueethonon.referentiel.enums.TypeMatiereEnum;
import org.mosqueethonon.referentiel.v1.dto.MatiereDto;
import org.mosqueethonon.referentiel.v1.dto.TraductionDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MatiereService {

    Map<TypeMatiereEnum, List<TraductionDto>> findAll();

    Optional<MatiereEntity> findByCode(MatiereEnum matiere);

}
