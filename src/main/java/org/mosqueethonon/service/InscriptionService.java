package org.mosqueethonon.service;

import org.mosqueethonon.service.criteria.InscriptionCriteria;
import org.mosqueethonon.v1.dto.InscriptionDto;
import org.mosqueethonon.v1.dto.InscriptionLightDto;

import java.time.LocalDate;
import java.util.List;

public interface InscriptionService {

    public InscriptionDto savePersonne(InscriptionDto personne);
    public InscriptionDto findInscriptionById(Long id);
    public List<Long> validateInscriptions(List<Long> ids);

    public List<Long> deleteInscriptions(List<Long> ids);
}
