package org.mosqueethonon.v1.dto.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.mosqueethonon.annotations.DataBaseParam;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.enums.ParamNameEnum;

import java.time.LocalDate;

@Data
public class ParamsDto {

    @DataBaseParam(name = ParamNameEnum.REINSCRIPTION_ENABLED)
    private boolean reinscriptionPrioritaire;
    @DataBaseParam(name = ParamNameEnum.INSCRIPTION_ENFANT_ENABLED_FROM_DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate inscriptionEnfantEnabledFromDate;
    @DataBaseParam(name = ParamNameEnum.SEND_EMAIL_ENABLED)
    private boolean sendMailEnabled;
    @DataBaseParam(name = ParamNameEnum.INSCRIPTION_ADULTE_ENABLED_FROM_DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate inscriptionAdulteEnabledFromDate;

}
