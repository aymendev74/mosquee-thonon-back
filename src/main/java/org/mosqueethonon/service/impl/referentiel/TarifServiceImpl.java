package org.mosqueethonon.service.impl.referentiel;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.mosqueethonon.repository.TarifRepository;
import org.mosqueethonon.repository.specifications.TarifEntitySpecifications;
import org.mosqueethonon.service.referentiel.TarifService;
import org.mosqueethonon.service.criteria.TarifCriteria;
import org.mosqueethonon.v1.dto.referentiel.TarifDto;
import org.mosqueethonon.v1.mapper.referentiel.TarifMapper;
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
