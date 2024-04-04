package org.mosqueethonon.v1.criterias;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AdhesionCriteria {

    private String nom;
    private String prenom;
    private StatutInscription statut;
    private BigDecimal montant;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate dateInscription;

}
