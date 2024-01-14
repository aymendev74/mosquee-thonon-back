package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.PeriodeEntity;
import org.mosqueethonon.entity.PeriodeInfoEntity;
import org.mosqueethonon.repository.PeriodeInfoRepository;
import org.mosqueethonon.repository.PeriodeRepository;
import org.mosqueethonon.service.PeriodeService;
import org.mosqueethonon.v1.dto.PeriodeDto;
import org.mosqueethonon.v1.dto.PeriodeInfoDto;
import org.mosqueethonon.v1.mapper.PeriodeInfoMapper;
import org.mosqueethonon.v1.mapper.PeriodeMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PeriodeServiceImpl implements PeriodeService {

    private PeriodeInfoRepository periodeInfoRepository;
    private PeriodeRepository periodeRepository;
    private PeriodeInfoMapper periodeInfoMapper;
    private PeriodeMapper periodeMapper;

    @Override
    public List<PeriodeInfoDto> findAllPeriodes() {
        List<PeriodeInfoEntity> periodeEntities = this.periodeInfoRepository.findAll();
        if(!CollectionUtils.isEmpty(periodeEntities)) {
            return periodeEntities.stream().map(this.periodeInfoMapper::fromEntityToDto).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public PeriodeDto savePeriode(PeriodeDto periode) {
        PeriodeEntity periodeEntity = this.periodeRepository.save(this.periodeMapper.fromDtoToEntity(periode));
        return this.periodeMapper.fromEntityToDto(periodeEntity);
    }
}
