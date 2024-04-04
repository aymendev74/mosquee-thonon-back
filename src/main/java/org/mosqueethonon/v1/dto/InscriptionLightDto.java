package org.mosqueethonon.v1.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.time.LocalDate;

@Data
public class InscriptionLightDto {

    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate dateInscription;
    private Long idInscription;
    private String nom;
    private String prenom;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate dateNaissance;
    private String niveau;
    private String telephone;
    private String mobile;
    private StatutInscription statut;
    private String ville;
    private String noInscription;

}
