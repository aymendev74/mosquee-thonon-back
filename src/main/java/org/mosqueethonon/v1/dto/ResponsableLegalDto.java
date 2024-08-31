package org.mosqueethonon.v1.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.mosqueethonon.entity.Signature;

@Data
@SuperBuilder
@NoArgsConstructor
public class ResponsableLegalDto implements IMailObject {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String mobile;
    private String numeroEtRue;
    private Integer codePostal;
    private String ville;
    private Long idTarif;
    private Boolean adherent;
    private Boolean autorisationAutonomie;
    private Boolean autorisationMedia;
    private String nomAutre;
    private String prenomAutre;
    private String lienParente;
    private String telephoneAutre;
    private Signature signature;

}
