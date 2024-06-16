package org.mosqueethonon.v1.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InscriptionLightDto {

    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_TIME_FORMAT)
    private LocalDateTime dateInscription;
    private Long idInscription;
    private String nom;
    private String prenom;
    private String nomResponsableLegal;
    private String prenomResponsableLegal;
    private String nomContactUrgence;
    private String prenomContactUrgence;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate dateNaissance;
    private String niveau;
    private String niveauInterne;
    private String mobile;
    private String mobileContactUrgence;
    private Boolean autorisationAutonomie;
    private Boolean autorisationMedia;
    private StatutInscription statut;
    private String ville;
    private String noInscription;

}
