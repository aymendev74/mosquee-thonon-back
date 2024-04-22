package org.mosqueethonon.service;

import org.mosqueethonon.v1.dto.ParamDto;

public interface ParamService {

    boolean isReinscriptionPrioritaireEnabled();

    void saveParam(ParamDto paramDto);

}
