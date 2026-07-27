package org.mosqueethonon.inscription.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.inscription.entity.InscriptionLightEntity;
import org.mosqueethonon.inscription.repository.InscriptionLightRepository;
import org.mosqueethonon.inscription.repository.specification.InscriptionLightEntitySpecifications;
import org.mosqueethonon.inscription.service.InscriptionLightService;
import org.mosqueethonon.inscription.v1.criteria.InscriptionCriteria;
import org.mosqueethonon.inscription.v1.dto.InscriptionLightDto;
import org.mosqueethonon.inscription.v1.mapper.InscriptionLightMapper;
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
