package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.ParamEntity;
import org.mosqueethonon.enums.ParamNameEnum;
import org.mosqueethonon.enums.ParamTypeEnum;
import org.mosqueethonon.params.BooleanParamValueParser;
import org.mosqueethonon.repository.ParamRepository;
import org.mosqueethonon.service.ParamService;
import org.mosqueethonon.v1.dto.ParamDto;
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

    @Override
    public void saveParam(ParamDto paramDto) {
        ParamEntity param = this.paramRepository.findByName(paramDto.getName());
        if(!this.isValidParamValue(paramDto.getValue(), paramDto.getName().getType())) {
            throw new IllegalArgumentException("La valeur " + paramDto.getValue() + " n'est pas valide " +
                    "pour les paramètres du type : " + param.getName().getType());
        }
        if (param == null) {
            param = new ParamEntity();
            param.setName(paramDto.getName());
        }
        param.setValue(paramDto.getValue());
        this.paramRepository.save(param);
    }

    private boolean isValidParamValue(String value, ParamTypeEnum type) {
        return switch (type) {
            case BOOLEAN -> value != null && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"));
            default -> throw new IllegalArgumentException("Aucune règle de validation pour les paramètres de type : " + type);
        };
    }
}
