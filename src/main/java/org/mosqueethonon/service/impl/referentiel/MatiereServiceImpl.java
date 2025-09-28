package org.mosqueethonon.service.impl.referentiel;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.mosqueethonon.entity.referentiel.MatiereEntity;
import org.mosqueethonon.enums.MatiereEnum;
import org.mosqueethonon.enums.TypeMatiereEnum;
import org.mosqueethonon.repository.MatiereRepository;
import org.mosqueethonon.service.referentiel.MatiereService;
import org.mosqueethonon.service.referentiel.TraductionService;
import org.mosqueethonon.v1.dto.referentiel.TraductionDto;
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
