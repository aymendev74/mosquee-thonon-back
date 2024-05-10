package org.mosqueethonon.v1.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.mosqueethonon.configuration.APIDateFormats;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SignatureDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_TIME_FORMAT)
    private LocalDateTime dateCreation;
    private String visaCreation;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_TIME_FORMAT)
    private LocalDateTime dateModification;
    private String visaModification;

}
