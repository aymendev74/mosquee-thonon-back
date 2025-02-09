package org.mosqueethonon.v1.dto.referentiel;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class MatiereDto {

    private Long id;
    private String libelle;

}
