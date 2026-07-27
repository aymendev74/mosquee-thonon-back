package org.mosqueethonon.adhesion.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.adhesion.entity.AdhesionLightEntity;
import org.mosqueethonon.adhesion.repository.AdhesionLightRepository;
import org.mosqueethonon.adhesion.repository.specification.AdhesionLightEntitySpecifications;
import org.mosqueethonon.adhesion.service.AdhesionLightService;
import org.mosqueethonon.adhesion.v1.criteria.AdhesionCriteria;
import org.mosqueethonon.adhesion.v1.dto.AdhesionLightDto;
import org.mosqueethonon.adhesion.v1.mapper.AdhesionLightMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdhesionLightServiceImpl implements AdhesionLightService {

    private AdhesionLightRepository adhesionLightRepository;
    private AdhesionLightMapper adhesionLightMapper;

    @Override
    public List<AdhesionLightDto> findAdhesionsLightByCriteria(AdhesionCriteria criteria) {
        List<AdhesionLightEntity> adhesions = this.adhesionLightRepository.findAll(AdhesionLightEntitySpecifications.withCriteria(criteria));
        if(!CollectionUtils.isEmpty(adhesions)) {
            return adhesions.stream().map(this.adhesionLightMapper::fromEntityToDto).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
