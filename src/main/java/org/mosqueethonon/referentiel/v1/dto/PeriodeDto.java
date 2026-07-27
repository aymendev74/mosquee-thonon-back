package org.mosqueethonon.referentiel.v1.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.mosqueethonon.common.config.APIDateFormats;

import java.time.LocalDate;

@Data
public class PeriodeDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate dateDebut;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate dateFin;
    private Integer anneeDebut;
    private Integer anneeFin;
    private Integer nbMaxInscription;
    private String application;

}
