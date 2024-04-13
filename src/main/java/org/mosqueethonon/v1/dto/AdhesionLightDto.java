package org.mosqueethonon.v1.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.v1.enums.StatutInscription;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AdhesionLightDto {

    private Long id;
    private String nom;
    private String prenom;
    private String ville;
    private StatutInscription statut;
    private BigDecimal montant;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_TIME_FORMAT)
    private LocalDateTime dateInscription;

}
