package org.mosqueethonon.service.enseignant;

import org.mosqueethonon.v1.dto.enseignant.EnseignantDto;

import java.util.List;

public interface EnseignantService {

    List<EnseignantDto> findAllEnseignants();

    EnseignantDto createEnseignantDto(EnseignantDto enseignantDto);

    EnseignantDto updateEnseignantDto(Long id, EnseignantDto enseignantDto);

    boolean deleteEnseignant(Long id);

}
