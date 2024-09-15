package org.mosqueethonon.service.impl.inscription;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.inscription.InscriptionLightEntity;
import org.mosqueethonon.repository.InscriptionLightRepository;
import org.mosqueethonon.repository.specifications.InscriptionLightEntitySpecifications;
import org.mosqueethonon.service.inscription.InscriptionLightService;
import org.mosqueethonon.v1.criterias.InscriptionCriteria;
import org.mosqueethonon.v1.dto.inscription.InscriptionLightDto;
import org.mosqueethonon.v1.mapper.inscription.InscriptionLightMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InscriptionLightServiceImpl implements InscriptionLightService {

    private InscriptionLightRepository inscriptionLightRepository;
    private InscriptionLightMapper inscriptionEnfantLightMapper;

    @Override
    public List<InscriptionLightDto> findInscriptionsEnfantLightByCriteria(InscriptionCriteria criteria) {
        List<InscriptionLightEntity> personnes = this.inscriptionLightRepository.findAll(InscriptionLightEntitySpecifications.withCriteria(criteria));
        if(!CollectionUtils.isEmpty(personnes)) {
            return personnes.stream().map(this.inscriptionEnfantLightMapper::fromEntityToDto).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
