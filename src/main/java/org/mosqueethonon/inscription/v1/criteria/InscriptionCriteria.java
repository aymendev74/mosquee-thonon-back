package org.mosqueethonon.inscription.v1.criteria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.referentiel.enums.NiveauInterneEnum;
import org.mosqueethonon.inscription.enums.NiveauScolaireEnum;
import org.mosqueethonon.inscription.enums.StatutInscription;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InscriptionCriteria {

    private String nom;
    private String prenom;
    private String telephone;
    private StatutInscription statut;
    private Long nbDerniersJours;
    // Annotation permettant à spring de désérialiser à partir d'une string (uniquement utilisé sur les request params => GET)
    // jackson est utilisé lui pour les request body (POST)
    @DateTimeFormat(pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate dateInscription;
    private List<NiveauScolaireEnum> niveaux;
    private List<NiveauInterneEnum> niveauxInternes;
    private String noInscription;
    private Long idPeriode;
    private String type;
    private Boolean reinscription;

}
