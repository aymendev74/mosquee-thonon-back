package org.mosqueethonon.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.mosqueethonon.entity.ParamEntity;
import org.mosqueethonon.enums.ParamNameEnum;
import org.mosqueethonon.enums.ParamTypeEnum;
import org.mosqueethonon.params.BooleanParamValueParser;
import org.mosqueethonon.repository.ParamRepository;
import org.mosqueethonon.service.ParamService;
import org.mosqueethonon.v1.dto.ParamDto;
import org.mosqueethonon.v1.dto.ParamsDto;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public String getAnneeScolaireEnCours() {
        ParamEntity param = this.paramRepository.findByName(ParamNameEnum.ANNEE_SCOLAIRE);
        if (param == null) {
            return "!!! NON DEFINIE !!!";
        }
        return param.getValue();
    }

    @Override
    @Transactional
    public void saveParam(List<ParamDto> paramDtos) {
        for(ParamDto paramDto : paramDtos) {
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
    }

    private boolean isValidParamValue(String value, ParamTypeEnum type) {
        return switch (type) {
            case BOOLEAN -> value != null && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"));
            case STRING -> true;
            default -> throw new IllegalArgumentException("Aucune règle de validation pour les paramètres de type : " + type);
        };
    }

    @Override
    public ParamsDto getParams() {
        ParamsDto paramsDto = new ParamsDto();
        List<ParamEntity> params = this.paramRepository.findAll();
        if(CollectionUtils.isNotEmpty(params)) {
            for(ParamEntity param : params) {
                if(param.getName() == ParamNameEnum.REINSCRIPTION_ENABLED) {
                    paramsDto.setReinscriptionPrioritaire(this.booleanParamValueParser.getValue(param.getValue()));
                }
                if(param.getName() == ParamNameEnum.ANNEE_SCOLAIRE) {
                    paramsDto.setAnneeScolaire(param.getValue());
                }
            }
        }
        return paramsDto;
    }
}
