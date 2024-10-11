package org.mosqueethonon.service.param;

import org.mosqueethonon.v1.dto.param.ParamDto;
import org.mosqueethonon.v1.dto.param.ParamsDto;

import java.util.List;

public interface ParamService {

    boolean isReinscriptionPrioritaireEnabled();

    void saveParam(List<ParamDto> paramDtos);

    boolean isInscriptionEnabled();

    ParamsDto getParams();

    boolean isSendEmailEnabled();
}
