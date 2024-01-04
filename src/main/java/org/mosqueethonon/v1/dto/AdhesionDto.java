package org.mosqueethonon.v1.dto;

import lombok.Data;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AdhesionDto {

    private Long id;
    private String titre;
    private String nom;
    private String prenom;
    private String dateNaissance;
    private Long idTarif;
    private String telephone;
    private String mobile;
    private String email;
    private String numeroEtRue;
    private Integer codePostal;
    private String ville;
    private BigDecimal montantAutre;
    private StatutInscription statut;
    private String dateInscription;
    private SignatureDto signature;

}
