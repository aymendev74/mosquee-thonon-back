package org.mosqueethonon.inscription.v1.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.mosqueethonon.common.config.APIDateFormats;
import org.mosqueethonon.referentiel.enums.NiveauInterneEnum;
import org.mosqueethonon.inscription.enums.NiveauScolaireEnum;
import org.mosqueethonon.inscription.enums.ResultatEnum;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EleveDto {

    private Long id;
    private String nom;
    private String prenom;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate dateNaissance;
    private NiveauScolaireEnum niveau;
    private NiveauInterneEnum niveauInterne;
    private ResultatEnum resultat;
    private Long classeId;

}
