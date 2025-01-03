package org.mosqueethonon.v1.dto.inscription;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.NiveauScolaireEnum;
import org.mosqueethonon.enums.ResultatEnum;

import java.time.LocalDate;

@Data
@Builder
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
