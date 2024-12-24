package org.mosqueethonon.v1.criterias;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.enums.NiveauInterneEnum;

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
