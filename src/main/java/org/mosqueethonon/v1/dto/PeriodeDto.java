package org.mosqueethonon.v1.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.mosqueethonon.configuration.APIDateFormats;

import java.time.LocalDate;

@Data
public class PeriodeDto {

    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate dateDebut;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate dateFin;
    private Integer nbMaxInscription;
    private SignatureDto signature;
    private String application;

}
