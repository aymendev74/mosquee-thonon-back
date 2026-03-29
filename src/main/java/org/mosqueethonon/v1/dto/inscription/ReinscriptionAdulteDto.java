package org.mosqueethonon.v1.dto.inscription;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.enums.MatiereEnum;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.SexeEnum;
import org.mosqueethonon.enums.StatutProfessionnelEnum;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class ReinscriptionAdulteDto {

    private String nom;
    private String prenom;
    private String email;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate dateNaissance;
    private String mobile;
    private String numeroEtRue;
    private Integer codePostal;
    private String ville;
    private NiveauInterneEnum niveauInterne;
    private SexeEnum sexe;
    private String anneeScolaire;
    private StatutProfessionnelEnum statutProfessionnel;
    private List<MatiereEnum> matieres;

}
