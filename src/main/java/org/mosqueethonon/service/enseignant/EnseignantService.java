package org.mosqueethonon.service.enseignant;

import org.mosqueethonon.v1.dto.enseignant.EnseignantDto;

import java.util.List;

public interface EnseignantService {

    List<EnseignantDto> findAllEnseignants();

    EnseignantDto createEnseignant(EnseignantDto enseignantDto);

    EnseignantDto updateEnseignant(Long id, EnseignantDto enseignantDto);

    boolean deleteEnseignant(Long id);

}
