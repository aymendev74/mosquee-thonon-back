package org.mosqueethonon.v1.dto.classe;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.mosqueethonon.configuration.APIDateFormats;

import java.time.LocalDate;
import java.util.List;

@Data
public class FeuillePresenceDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate date;
    private List<PresenceEleveDto> presenceEleves;

}
