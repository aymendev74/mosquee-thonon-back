package org.mosqueethonon.param.service;

import org.mosqueethonon.param.v1.dto.ParamDto;
import org.mosqueethonon.param.v1.dto.ParamsDto;

import java.util.List;

public interface ParamService {

    boolean isReinscriptionPrioritaireEnabled();

    void saveParam(List<ParamDto> paramDtos);

    boolean isInscriptionEnfantEnabled();

    boolean isInscriptionAdulteEnabled();

    ParamsDto getParams();

    boolean isSendEmailEnabled();
}
