package org.mosqueethonon.v1.criterias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdhesionCriteria {

    private String nom;
    private String prenom;
    private StatutInscription statut;
    private BigDecimal montant;
    // Annotation permettant à spring de désérialiser à partir d'une string (uniquement utilisé sur les request params => GET)
    // jackson est utilisé lui pour les request body (POST)
    @DateTimeFormat(pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate dateInscription;

}
