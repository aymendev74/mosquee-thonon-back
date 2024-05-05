package org.mosqueethonon.v1.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.entity.Signature;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
public class InscriptionDto {

    private Long id;
    private StatutInscription statut;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_TIME_FORMAT)
    private LocalDateTime dateInscription;
    private ResponsableLegalDto responsableLegal;
    private List<EleveDto> eleves;
    private String noInscription;
    private Integer noPositionAttente;
    private Signature signature;
    private String anneeScolaire;
    private BigDecimal montantTotal;

}
