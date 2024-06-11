package org.mosqueethonon.service;

import org.mosqueethonon.v1.dto.ParamDto;
import org.mosqueethonon.v1.dto.ParamsDto;

import java.util.List;

public interface ParamService {

    boolean isReinscriptionPrioritaireEnabled();

    String getAnneeScolaireEnCours();

    void saveParam(List<ParamDto> paramDtos);

    boolean isInscriptionEnabled();

    ParamsDto getParams();

}
