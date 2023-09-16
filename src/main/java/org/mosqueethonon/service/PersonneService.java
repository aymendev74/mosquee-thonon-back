package org.mosqueethonon.service;

import org.mosqueethonon.service.criteria.PersonneCriteria;
import org.mosqueethonon.v1.dto.PersonneDto;

import java.util.List;

public interface PersonneService {

    public PersonneDto savePersonne(PersonneDto personne);
    public List<PersonneDto> findPersonneByCriteria(PersonneCriteria criteria);

}
