package org.mosqueethonon.v1.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.configuration.APIDateFormats;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class InscriptionInfosDto {

    private Boolean adherent;
    private Integer nbEleves;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate atDate;

}
