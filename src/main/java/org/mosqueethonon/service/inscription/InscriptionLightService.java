package org.mosqueethonon.service.inscription;

import org.mosqueethonon.v1.criterias.InscriptionCriteria;
import org.mosqueethonon.v1.dto.inscription.InscriptionLightDto;

import java.util.List;

public interface InscriptionLightService {

    public List<InscriptionLightDto> findInscriptionsEnfantLightByCriteria(InscriptionCriteria criteria);


}
