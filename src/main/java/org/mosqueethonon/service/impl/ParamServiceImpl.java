package org.mosqueethonon.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.entity.ParamEntity;
import org.mosqueethonon.enums.ParamNameEnum;
import org.mosqueethonon.enums.ParamTypeEnum;
import org.mosqueethonon.params.BooleanParamValueParser;
import org.mosqueethonon.params.DateParamValueParser;
import org.mosqueethonon.repository.ParamRepository;
import org.mosqueethonon.service.ParamService;
import org.mosqueethonon.v1.dto.ParamDto;
import org.mosqueethonon.v1.dto.ParamsDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@AllArgsConstructor
@Service
public class ParamServiceImpl implements ParamService {

    private ParamRepository paramRepository;
    private BooleanParamValueParser booleanParamValueParser;

    private DateParamValueParser dateParamValueParser;

    @Override
    public boolean isReinscriptionPrioritaireEnabled() {
        return this.findParamAsBoolean(ParamNameEnum.REINSCRIPTION_ENABLED);
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
            case DATE -> StringUtils.EMPTY.equals(value) || this.isValideDate(value);
            default -> throw new IllegalArgumentException("Aucune règle de validation pour les paramètres de type : " + type);
        };
    }

    private boolean isValideDate(String value) {
        try {
            LocalDate.parse(value, DateTimeFormatter.ofPattern(APIDateFormats.DATE_FORMAT));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
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
                if(param.getName() == ParamNameEnum.INSCRIPTION_ENABLED_FROM_DATE) {
                    paramsDto.setInscriptionEnabledFromDate(this.dateParamValueParser.getValue(param.getValue()));
                }
            }
        }
        return paramsDto;
    }

    @Override
    public boolean isInscriptionEnabled() {
        LocalDate inscriptionEnabledFromDate = this.findParamAsLocalDate(ParamNameEnum.INSCRIPTION_ENABLED_FROM_DATE);
        return inscriptionEnabledFromDate != null && !inscriptionEnabledFromDate.isAfter(LocalDate.now());
    }

    private boolean findParamAsBoolean(ParamNameEnum paramName) {
        ParamEntity param = this.paramRepository.findByName(paramName);
        if (param == null) {
            return false;
        }
        return this.booleanParamValueParser.getValue(param.getValue());
    }

    private LocalDate findParamAsLocalDate(ParamNameEnum paramName) {
        ParamEntity param = this.paramRepository.findByName(paramName);
        if (param == null || StringUtils.EMPTY.equals(param.getValue())) {
            return null;
        }
        return this.dateParamValueParser.getValue(param.getValue());
    }

}
