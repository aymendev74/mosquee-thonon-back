package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.ParamEntity;
import org.mosqueethonon.enums.ParamNameEnum;
import org.mosqueethonon.params.BooleanParamValueParser;
import org.mosqueethonon.repository.ParamRepository;
import org.mosqueethonon.service.ParamService;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ParamServiceImpl implements ParamService {

    private ParamRepository paramRepository;
    private BooleanParamValueParser booleanParamValueParser;

    @Override
    public boolean isReinscriptionPrioritaireEnabled() {
        ParamEntity param = this.paramRepository.findByName(ParamNameEnum.REINSCRIPTION_ENABLED);
        if (param == null) {
            return false;
        }
        return this.booleanParamValueParser.getValue(param.getValue());
    }
}
