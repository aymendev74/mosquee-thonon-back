package org.mosqueethonon.service.impl.enseignant;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.mosqueethonon.entity.classe.EnseignantEntity;
import org.mosqueethonon.repository.EnseignantRepository;
import org.mosqueethonon.service.enseignant.EnseignantService;
import org.mosqueethonon.v1.dto.enseignant.EnseignantDto;
import org.mosqueethonon.v1.mapper.enseignant.EnseignantMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@NoArgsConstructor
public class EnseignantServiceImpl implements EnseignantService {

    private EnseignantRepository enseignantRepository;
    private EnseignantMapper enseignantMapper;

    @Override
    public List<EnseignantDto> findAllEnseignants() {
        return this.enseignantMapper.fromEntityToDto(this.enseignantRepository.findAll());
    }

    @Override
    public EnseignantDto createEnseignantDto(EnseignantDto enseignantDto) {
        return this.enseignantMapper.fromEntityToDto(this.enseignantRepository.save(this.enseignantMapper.fromDtoToEntity(enseignantDto)));
    }

    @Override
    public EnseignantDto updateEnseignantDto(Long id, EnseignantDto enseignantDto) {
        EnseignantEntity enseignantEntity = this.enseignantRepository.findById(id).orElse(null);
        if(enseignantEntity == null) {
            throw new IllegalArgumentException("Enseignant inexistant pour l'id : " + id);
        }
        this.enseignantMapper.updateEnseignantEntity(enseignantDto, enseignantEntity);
        return this.enseignantMapper.fromEntityToDto(this.enseignantRepository.save(enseignantEntity));
    }
}