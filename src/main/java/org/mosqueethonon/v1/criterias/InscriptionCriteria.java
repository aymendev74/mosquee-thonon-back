package org.mosqueethonon.v1.criterias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.NiveauScolaireEnum;
import org.mosqueethonon.v1.enums.StatutInscription;
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

}
