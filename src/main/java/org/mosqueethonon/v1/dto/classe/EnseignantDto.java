package org.mosqueethonon.v1.dto.classe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnseignantDto {

    private Long id;
    private String nomPrenom;

}
