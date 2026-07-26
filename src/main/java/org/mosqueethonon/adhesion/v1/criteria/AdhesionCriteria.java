package org.mosqueethonon.adhesion.v1.criteria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.common.config.APIDateFormats;
import org.mosqueethonon.inscription.enums.StatutInscriptionEnum;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdhesionCriteria {

    private String nom;
    private String prenom;
    private StatutInscriptionEnum statut;
    private BigDecimal montant;
    // Annotation permettant à spring de désérialiser à partir d'une string (uniquement utilisé sur les request params => GET)
    // jackson est utilisé lui pour les request body (POST)
    @DateTimeFormat(pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate dateInscription;

}
