package org.mosqueethonon.v1.dto.classe;

import lombok.Data;
import org.mosqueethonon.enums.NiveauInterneEnum;
import java.util.List;

@Data
public class ClasseDto {

    private Long id;
    private String libelle;
    private NiveauInterneEnum niveau;
    private List<EnseignantDto> enseignants;
    private List<LienClasseEleveDto> liensClasseEleves;
    private Integer debutAnneeScolaire;
    private Integer finAnneeScolaire;
    private List<ClasseActiviteDto> activites;

}
