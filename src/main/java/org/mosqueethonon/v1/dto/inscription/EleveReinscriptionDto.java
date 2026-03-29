package org.mosqueethonon.v1.dto.inscription;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.mosqueethonon.enums.NiveauScolaireEnum;

@Data
@SuperBuilder
@NoArgsConstructor
public class EleveReinscriptionDto {

    private Long id;
    private NiveauScolaireEnum niveau;

}
