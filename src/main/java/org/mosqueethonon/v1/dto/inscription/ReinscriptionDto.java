package org.mosqueethonon.v1.dto.inscription;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
public class ReinscriptionDto {

    private ResponsableLegalDto responsableLegal;
    private List<Long> elevesIds;

}
