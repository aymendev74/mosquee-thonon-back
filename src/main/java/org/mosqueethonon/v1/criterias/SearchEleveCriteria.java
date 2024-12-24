package org.mosqueethonon.v1.criterias;

import lombok.Data;
import org.mosqueethonon.enums.AffectationEleveEnum;

@Data
public class SearchEleveCriteria {

    private Integer anneeDebut;
    private Integer anneeFin;
    private AffectationEleveEnum affectation;

}
