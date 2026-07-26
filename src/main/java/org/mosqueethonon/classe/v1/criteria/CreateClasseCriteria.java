package org.mosqueethonon.classe.v1.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.referentiel.enums.NiveauInterneEnum;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateClasseCriteria {

    private Integer debutAnneeScolaire;
    private Integer finAnneeScolaire;
    private Integer nbMaxEleveParClasse;

}
