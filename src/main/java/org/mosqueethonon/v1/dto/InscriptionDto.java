package org.mosqueethonon.v1.dto;

import lombok.Data;
import org.mosqueethonon.v1.enums.StatutInscription;

@Data
public class InscriptionDto {

    private Long id;
    private String nom;
    private String prenom;
    private String dateNaissance;
    private String telephone;
    private String email;
    private String sexe;
    private String numeroEtRue;
    private Integer codePostal;
    private String ville;
    private StatutInscription statut;
    private SignatureDto signature;

}
