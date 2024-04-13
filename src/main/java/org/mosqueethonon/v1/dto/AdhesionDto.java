package org.mosqueethonon.v1.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
public class AdhesionDto extends MailObjectDto {

    private Long id;
    private String titre;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate dateNaissance;
    private Long idTarif;
    private String telephone;
    private String mobile;
    private String numeroEtRue;
    private Integer codePostal;
    private String ville;
    private BigDecimal montantAutre;
    private StatutInscription statut;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_TIME_FORMAT)
    private LocalDateTime dateInscription;
    private BigDecimal montant;
    private Integer noMembre;
    private SignatureDto signature;

}
