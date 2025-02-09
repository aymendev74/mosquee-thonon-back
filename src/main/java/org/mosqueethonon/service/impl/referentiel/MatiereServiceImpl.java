package org.mosqueethonon.service.impl.referentiel;

import lombok.AllArgsConstructor;
import org.mosqueethonon.repository.MatiereRepository;
import org.mosqueethonon.service.referentiel.MatiereService;
import org.mosqueethonon.v1.dto.referentiel.MatiereDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class MatiereServiceImpl implements MatiereService {

    private MatiereRepository matiereRepository;

    @Override
    public List<MatiereDto> findAllMatieres() {
        return this.matiereRepository.findAll().stream().map(matiere -> MatiereDto.builder().id(matiere.getId())
                .libelle(matiere.getLibelle()).build()).toList();
    }
}
