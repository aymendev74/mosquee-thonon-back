package org.mosqueethonon.service.criteria;

import lombok.Data;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.time.LocalDate;

@Data
public class PersonneCriteria {

    private String nom;
    private String prenom;
    private String telephone;
    private StatutInscription statut;

}
