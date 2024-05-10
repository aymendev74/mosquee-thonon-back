package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.TarifEntity;
import org.mosqueethonon.repository.TarifRepository;
import org.mosqueethonon.repository.specifications.TarifEntitySpecifications;
import org.mosqueethonon.service.TarifService;
import org.mosqueethonon.service.criteria.TarifCriteria;
import org.mosqueethonon.v1.dto.TarifDto;
import org.mosqueethonon.v1.mapper.TarifMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TarifServiceImpl implements TarifService {

    private TarifRepository tarifRepository;
    private TarifMapper tarifMapper;

    @Override
    public List<TarifDto> findTarifByCriteria(TarifCriteria criteria) {
        List<TarifEntity> tarifsEntities = this.tarifRepository.findAll(TarifEntitySpecifications.withCriteria(criteria));
        if(tarifsEntities!=null) {
            return tarifsEntities.stream().map(this.tarifMapper::fromEntityToDto).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
