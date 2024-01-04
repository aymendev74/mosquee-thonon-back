package org.mosqueethonon.v1.dto;

import lombok.Data;
import org.mosqueethonon.v1.enums.StatutInscription;
import java.math.BigDecimal;

@Data
public class AdhesionLightDto {

    private Long id;
    private String nom;
    private String prenom;
    private String ville;
    private StatutInscription statut;
    private BigDecimal montant;
    private String dateInscription;

}
