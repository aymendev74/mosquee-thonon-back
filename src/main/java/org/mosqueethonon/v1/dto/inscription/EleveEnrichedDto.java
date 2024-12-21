package org.mosqueethonon.v1.dto.inscription;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.NiveauScolaireEnum;
import org.mosqueethonon.enums.ResultatEnum;

import java.time.LocalDate;

@Data
public class EleveEnrichedDto {

    private Long id;
    private String nom;
    private String prenom;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate dateNaissance;
    private NiveauInterneEnum niveauInterne;
    private String mobile;
    private String mobileContactUrgence;
    private Boolean autorisationAutonomie;
    private Boolean autorisationMedia;
    private String nomResponsableLegal;
    private String prenomResponsableLegal;
    private String nomContactUrgence;
    private String prenomContactUrgence;
    private ResultatEnum resultat;

}
