package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.PeriodeEntity;
import org.mosqueethonon.repository.PeriodeRepository;
import org.mosqueethonon.service.PeriodeService;
import org.mosqueethonon.v1.dto.PeriodeDto;
import org.mosqueethonon.v1.mapper.PeriodeMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PeriodeServiceImpl implements PeriodeService {

    private PeriodeRepository periodeRepository;
    private PeriodeMapper periodeMapper;

    @Override
    public List<PeriodeDto> findAllPeriodes() {
        List<PeriodeEntity> periodeEntities = this.periodeRepository.findAll();
        if(!CollectionUtils.isEmpty(periodeEntities)) {
            return periodeEntities.stream().map(this.periodeMapper::fromEntityToDto).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
