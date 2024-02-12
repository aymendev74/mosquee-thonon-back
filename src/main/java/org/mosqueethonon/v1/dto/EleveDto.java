package org.mosqueethonon.v1.dto;

import lombok.Data;
import org.mosqueethonon.entity.Signature;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.NiveauScolaireEnum;

@Data
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
