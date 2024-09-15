package org.mosqueethonon.service.impl.adhesion;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.adhesion.AdhesionLightEntity;
import org.mosqueethonon.repository.AdhesionLightRepository;
import org.mosqueethonon.repository.specifications.AdhesionLightEntitySpecifications;
import org.mosqueethonon.service.adhesion.AdhesionLightService;
import org.mosqueethonon.v1.criterias.AdhesionCriteria;
import org.mosqueethonon.v1.dto.adhesion.AdhesionLightDto;
import org.mosqueethonon.v1.mapper.adhesion.AdhesionLightMapper;
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
