package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.InscriptionLightEntity;
import org.mosqueethonon.repository.InscriptionLightRepository;
import org.mosqueethonon.repository.specifications.InscriptionLightEntitySpecifications;
import org.mosqueethonon.service.InscriptionEnfantLightService;
import org.mosqueethonon.v1.criterias.InscriptionCriteria;
import org.mosqueethonon.v1.dto.InscriptionEnfantLightDto;
import org.mosqueethonon.v1.mapper.InscriptionEnfantLightMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InscriptionEnfantLightServiceImpl implements InscriptionEnfantLightService {

    private InscriptionLightRepository inscriptionLightRepository;
    private InscriptionEnfantLightMapper inscriptionEnfantLightMapper;

    @Override
    public List<InscriptionEnfantLightDto> findInscriptionsEnfantLightByCriteria(InscriptionCriteria criteria) {
        List<InscriptionLightEntity> personnes = this.inscriptionLightRepository.findAll(InscriptionLightEntitySpecifications.withCriteria(criteria));
        if(!CollectionUtils.isEmpty(personnes)) {
            return personnes.stream().map(this.inscriptionEnfantLightMapper::fromEntityToDto).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
