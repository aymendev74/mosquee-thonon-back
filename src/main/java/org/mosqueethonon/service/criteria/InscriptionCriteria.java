package org.mosqueethonon.service.criteria;

import lombok.Data;
import org.mosqueethonon.v1.enums.StatutInscription;

@Data
public class InscriptionCriteria {

    private String nom;
    private String prenom;
    private String telephone;
    private StatutInscription statut;

}
