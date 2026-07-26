package org.mosqueethonon.inscription.v1.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.referentiel.enums.NiveauInterneEnum;
import org.mosqueethonon.inscription.enums.NiveauScolaireEnum;
import org.mosqueethonon.inscription.enums.ResultatEnum;

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
