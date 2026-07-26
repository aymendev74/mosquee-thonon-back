package org.mosqueethonon.tarif.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.tarif.entity.TarifEntity;
import org.mosqueethonon.tarif.repository.TarifRepository;
import org.mosqueethonon.tarif.repository.specification.TarifEntitySpecifications;
import org.mosqueethonon.tarif.service.TarifService;
import org.mosqueethonon.tarif.criteria.TarifCriteria;
import org.mosqueethonon.tarif.v1.dto.TarifDto;
import org.mosqueethonon.tarif.v1.mapper.TarifMapper;
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
        return tarifsEntities.stream().map(this.tarifMapper::fromEntityToDto).collect(Collectors.toList());
    }
}
