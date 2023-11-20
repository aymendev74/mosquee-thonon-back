package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.TarifEntity;
import org.mosqueethonon.repository.TarifRepository;
import org.mosqueethonon.service.TarifService;
import org.mosqueethonon.v1.dto.TarifDto;
import org.mosqueethonon.v1.mapper.TarifMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TarifServiceImpl implements TarifService {

    private TarifRepository tarifRepository;
    private TarifMapper tarifMapper;

    @Override
    public List<TarifDto> findAllTarifs() {
        List<TarifEntity> tarifEntities = this.tarifRepository.findAll();
        if(!CollectionUtils.isEmpty(tarifEntities)) {
            return tarifEntities.stream().map(this.tarifMapper::fromEntityToDto).collect(Collectors.toList());
        }
        return null;
    }

}
