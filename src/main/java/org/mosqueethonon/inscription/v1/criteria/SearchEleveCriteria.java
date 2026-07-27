package org.mosqueethonon.inscription.v1.criteria;

import lombok.Data;
import org.mosqueethonon.inscription.enums.AffectationEleveEnum;

@Data
public class SearchEleveCriteria {

    private Integer anneeDebut;
    private Integer anneeFin;
    private AffectationEleveEnum affectation;
    private boolean avecNiveau;

}
