package org.mosqueethonon.inscription.v1.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
public class ReinscriptionDto {

    private ResponsableLegalDto responsableLegal;
    private List<EleveReinscriptionDto> eleves;

}
