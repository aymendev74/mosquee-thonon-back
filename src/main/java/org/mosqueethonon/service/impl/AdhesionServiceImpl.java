package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.AdhesionEntity;
import org.mosqueethonon.repository.AdhesionRepository;
import org.mosqueethonon.service.AdhesionService;
import org.mosqueethonon.v1.dto.AdhesionDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.mapper.AdhesionMapper;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AdhesionServiceImpl implements AdhesionService {

    private AdhesionRepository adhesionRepository;

    private AdhesionMapper adhesionMapper;

    @Override
    public AdhesionDto saveAdhesion(AdhesionDto adhesionDto) {
        if(adhesionDto.getStatut() == null) {
            adhesionDto.setStatut(StatutInscription.PROVISOIRE);
        }
        AdhesionEntity adhesionEntity = this.adhesionRepository.save(this.adhesionMapper.fromDtoToEntity(adhesionDto));
        return this.adhesionMapper.fromEntityToDto(adhesionEntity);
    }

}
