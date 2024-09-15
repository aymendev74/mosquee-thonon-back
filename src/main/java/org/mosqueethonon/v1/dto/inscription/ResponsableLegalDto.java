package org.mosqueethonon.v1.dto.inscription;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.mosqueethonon.v1.dto.IMailObject;

@Data
@SuperBuilder
@NoArgsConstructor
public class ResponsableLegalDto implements IMailObject {

    private String nom;
    private String prenom;
    private String email;
    private String mobile;
    private String numeroEtRue;
    private Integer codePostal;
    private String ville;
    private Boolean adherent;
    private Boolean autorisationAutonomie;
    private Boolean autorisationMedia;
    private String nomAutre;
    private String prenomAutre;
    private String lienParente;
    private String telephoneAutre;

}
