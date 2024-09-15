package org.mosqueethonon.service.impl.param;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.mosqueethonon.annotations.DataBaseParam;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.entity.ParamEntity;
import org.mosqueethonon.enums.ParamNameEnum;
import org.mosqueethonon.enums.ParamTypeEnum;
import org.mosqueethonon.params.BooleanParamValueParser;
import org.mosqueethonon.params.DateParamValueParser;
import org.mosqueethonon.params.ParamParser;
import org.mosqueethonon.repository.ParamRepository;
import org.mosqueethonon.service.param.ParamService;
import org.mosqueethonon.v1.dto.param.ParamDto;
import org.mosqueethonon.v1.dto.param.ParamsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@AllArgsConstructor
@Service
public class ParamServiceImpl implements ParamService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParamServiceImpl.class);

    private ParamRepository paramRepository;
    private BooleanParamValueParser booleanParamValueParser;

    private DateParamValueParser dateParamValueParser;

    private ParamParser paramParser;

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
        Field[] paramFields = ParamsDto.class.getDeclaredFields();
        List<ParamEntity> params = this.paramRepository.findAll();
        for (Field field : paramFields) {
            DataBaseParam annotation = field.getAnnotation(DataBaseParam.class);
            if (annotation != null) {
                ParamNameEnum paramName = annotation.name();
                for (ParamEntity paramEntity : params) {
                    if (paramName == paramEntity.getName()) {
                        field.setAccessible(true);
                        setFieldValue(paramsDto, field, paramEntity.getValue());
                    }
                }
            }
        }
        return paramsDto;
    }

    private void setFieldValue(ParamsDto paramsDto, Field field, String value) {
        try {
            Object parsedValue = field.getType() == String.class ? value : this.paramParser.parseValue(value, field.getType());
            field.set(paramsDto, parsedValue);
        } catch (IllegalAccessException e) {
            LOGGER.error("Erreur lors de la récupération des paramètres. paramName = " + field.getName());
            throw new RuntimeException(e);
        }
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

    @Override
    public boolean isSendEmailEnabled() {
        return this.findParamAsBoolean(ParamNameEnum.SEND_EMAIL_ENABLED);
    }
}
