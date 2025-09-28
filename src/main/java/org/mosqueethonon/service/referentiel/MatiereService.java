package org.mosqueethonon.service.referentiel;

import org.mosqueethonon.entity.referentiel.MatiereEntity;
import org.mosqueethonon.enums.MatiereEnum;
import org.mosqueethonon.enums.TypeMatiereEnum;
import org.mosqueethonon.v1.dto.referentiel.MatiereDto;
import org.mosqueethonon.v1.dto.referentiel.TraductionDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MatiereService {

    Map<TypeMatiereEnum, List<TraductionDto>> findAll();

    Optional<MatiereEntity> findByCode(MatiereEnum matiere);

}
