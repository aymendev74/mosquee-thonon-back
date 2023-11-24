package org.mosqueethonon.v1.dto;

import lombok.Data;

import java.util.List;

@Data
public class InscriptionInfosDto {

    private ResponsableLegalDto responsableLegal;
    private List<EleveDto> eleves;

}
