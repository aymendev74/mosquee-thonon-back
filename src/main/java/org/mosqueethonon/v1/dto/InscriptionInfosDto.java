package org.mosqueethonon.v1.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InscriptionInfosDto {

    private ResponsableLegalDto responsableLegal;
    private List<EleveDto> eleves;

}
