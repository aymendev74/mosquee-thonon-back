package org.mosqueethonon.service.criteria;

import lombok.Data;
import org.mosqueethonon.enums.NiveauScolaireEnum;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.time.LocalDate;
import java.util.List;

@Data
public class InscriptionCriteria {

    private String nom;
    private String prenom;
    private String telephone;
    private StatutInscription statut;
    private Long nbDerniersJours;
    private String dateInscription;
    private List<NiveauScolaireEnum> niveaux;

}
