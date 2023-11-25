package org.mosqueethonon.v1.dto;

import lombok.Data;
import org.mosqueethonon.v1.enums.StatutInscription;

@Data
public class InscriptionLightDto {

    private Long id;
    private String dateInscription;
    private Long idInscription;
    private String nom;
    private String prenom;
    private String dateNaissance;
    private String niveau;
    private String telephone;
    private String mobile;
    private StatutInscription statut;
    private String ville;

}
