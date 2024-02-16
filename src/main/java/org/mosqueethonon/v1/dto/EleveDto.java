package org.mosqueethonon.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.entity.Signature;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.NiveauScolaireEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EleveDto {

    private Long id;
    private String nom;
    private String prenom;
    private String dateNaissance;
    private NiveauScolaireEnum niveau;
    private NiveauInterneEnum niveauInterne;
    private Long idTarif;
    private Signature signature;

}
