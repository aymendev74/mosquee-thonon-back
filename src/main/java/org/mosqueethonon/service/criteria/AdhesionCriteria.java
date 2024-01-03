package org.mosqueethonon.service.criteria;

import lombok.Data;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.math.BigDecimal;

@Data
public class AdhesionCriteria {

    private String nom;
    private String prenom;
    private StatutInscription statut;
    private BigDecimal montant;
    private String dateInscription;

}
