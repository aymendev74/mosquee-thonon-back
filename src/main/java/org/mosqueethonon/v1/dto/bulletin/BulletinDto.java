package org.mosqueethonon.v1.dto.bulletin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.mosqueethonon.configuration.APIDateFormats;

import java.time.LocalDate;
import java.util.List;

@Data
public class BulletinDto {

    private Long id;
    private Long idEleve;
    private String appreciation;
    private Integer nbAbsences;
    private Integer mois;
    private Integer annee;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate dateBulletin;
    private List<BulletinMatiereDto> bulletinMatieres;

}
