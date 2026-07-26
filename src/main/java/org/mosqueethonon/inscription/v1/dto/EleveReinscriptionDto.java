package org.mosqueethonon.inscription.v1.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.mosqueethonon.inscription.enums.NiveauScolaireEnum;

@Data
@SuperBuilder
@NoArgsConstructor
public class EleveReinscriptionDto {

    private Long id;
    private NiveauScolaireEnum niveau;

}
