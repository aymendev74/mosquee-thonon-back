package org.mosqueethonon.v1.dto.referentiel;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.mosqueethonon.configuration.APIDateFormats;

import java.time.LocalDate;

@Data
public class PeriodeInfoDto extends PeriodeDto {

    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate dateDebut;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate dateFin;
    private Integer anneeDebut;
    private Integer anneeFin;
    private Integer nbMaxInscription;
    private String application;
    private Boolean existInscription;
    private Boolean active;

}
