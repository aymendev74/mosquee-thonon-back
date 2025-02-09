package org.mosqueethonon.service.referentiel;

import org.mosqueethonon.v1.dto.referentiel.MatiereDto;

import java.util.List;

public interface MatiereService {

    List<MatiereDto> findAllMatieres();

}
