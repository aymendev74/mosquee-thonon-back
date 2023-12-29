package org.mosqueethonon.v1.dto;

import lombok.Data;
import org.mosqueethonon.entity.Signature;

@Data
public class ResponsableLegalDto {

    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private String mobile;
    private String email;
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
    private Signature signature;

}
