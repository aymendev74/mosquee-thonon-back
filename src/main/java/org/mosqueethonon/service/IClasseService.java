package org.mosqueethonon.service;

import org.mosqueethonon.entity.ClasseEntity;
import org.mosqueethonon.v1.criterias.CreateClasseCriteria;

public interface IClasseService {

    void createClasses(CreateClasseCriteria criteria);

}
