package org.mosqueethonon.referentiel.service.impl;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.mosqueethonon.referentiel.entity.MatiereEntity;
import org.mosqueethonon.referentiel.enums.MatiereEnum;
import org.mosqueethonon.referentiel.enums.TypeMatiereEnum;
import org.mosqueethonon.referentiel.repository.MatiereRepository;
import org.mosqueethonon.referentiel.service.MatiereService;
import org.mosqueethonon.referentiel.service.TraductionService;
import org.mosqueethonon.referentiel.v1.dto.TraductionDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class MatiereServiceImpl implements MatiereService {

    private MatiereRepository matiereRepository;

    private TraductionService traductionService;

    @Override
    public Map<TypeMatiereEnum, List<TraductionDto>> findAll() {
        Map<TypeMatiereEnum, List<TraductionDto>> matieresResult = new HashMap<>();
        List<MatiereEntity> matieresEntities = this.matiereRepository.findAll();
        if (CollectionUtils.isNotEmpty(matieresEntities)) {
            Map<TypeMatiereEnum, List<MatiereEntity>> matieresByType = matieresEntities.stream().collect(Collectors.groupingBy(MatiereEntity::getType));
            matieresByType.forEach((type, matieres) -> {
                List<TraductionDto> traductions = matieres.stream().map(this::mapMatiereEntityToTraduction).toList();
                matieresResult.put(type, traductions);
            });
        }
        return matieresResult;
    }

    private TraductionDto mapMatiereEntityToTraduction(MatiereEntity matiereEntity) {
        return this.traductionService.findTraductionByCleAndValeur("cdmaticode", matiereEntity.getCode().name());
    }

    @Override
    public Optional<MatiereEntity> findByCode(MatiereEnum matiere) {
        return this.matiereRepository.findByCode(matiere);
    }

}
