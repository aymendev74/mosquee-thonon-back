package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.InscriptionLightEntity;
import org.mosqueethonon.repository.InscriptionLightRepository;
import org.mosqueethonon.repository.specifications.InscriptionLightEntitySpecifications;
import org.mosqueethonon.service.InscriptionLightService;
import org.mosqueethonon.service.criteria.InscriptionCriteria;
import org.mosqueethonon.v1.dto.InscriptionLightDto;
import org.mosqueethonon.v1.mapper.InscriptionLightMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InscriptionLightServiceImpl implements InscriptionLightService {

    private InscriptionLightRepository inscriptionLightRepository;
    private InscriptionLightMapper inscriptionLightMapper;

    @Override
    public List<InscriptionLightDto> findInscriptionsLightByCriteria(InscriptionCriteria criteria) {
        List<InscriptionLightEntity> personnes = this.inscriptionLightRepository.findAll(InscriptionLightEntitySpecifications.withCriteria(criteria));
        if(!CollectionUtils.isEmpty(personnes)) {
            return personnes.stream().map(this.inscriptionLightMapper::fromEntityToDto).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}