package org.mosqueethonon.inscription.v1.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.configuration.APIDateFormats;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InscriptionEnfantInfosDto {

    private Boolean adherent;
    private Integer nbEleves;

}
