package org.mosqueethonon.service;

import org.mosqueethonon.service.criteria.InscriptionCriteria;
import org.mosqueethonon.v1.dto.InscriptionDto;

import java.util.List;

public interface InscriptionService {

    public InscriptionDto savePersonne(InscriptionDto personne);
    public List<InscriptionDto> findPersonneByCriteria(InscriptionCriteria criteria);
    public InscriptionDto findInscriptionById(Long id);
    public List<Long> validateInscriptions(List<Long> ids);

    public List<Long> deleteInscriptions(List<Long> ids);
}
