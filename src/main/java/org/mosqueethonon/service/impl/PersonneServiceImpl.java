package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.PersonneEntity;
import org.mosqueethonon.repository.PersonneRepository;
import org.mosqueethonon.repository.specifications.PersonneEntitySpecifications;
import org.mosqueethonon.service.PersonneService;
import org.mosqueethonon.service.criteria.PersonneCriteria;
import org.mosqueethonon.v1.dto.PersonneDto;
import org.mosqueethonon.v1.mapper.PersonneMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PersonneServiceImpl implements PersonneService {

    private PersonneRepository personneRepository;
    private PersonneMapper personneMapper;

    @Override
    public PersonneDto savePersonne(PersonneDto personne) {
        PersonneEntity entity = this.personneMapper.fromDtoToEntity(personne);
        entity = this.personneRepository.save(entity);
        return this.personneMapper.fromEntityToDto(entity);
    }

    @Override
    public List<PersonneDto> findPersonneByCriteria(PersonneCriteria criteria) {
        List<PersonneEntity> personnes = this.personneRepository.findAll(PersonneEntitySpecifications.withCriteria(criteria));
        if(!CollectionUtils.isEmpty(personnes)) {
            return personnes.stream().map(this.personneMapper::fromEntityToDto).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public PersonneDto findInscriptionById(Long id) {
        PersonneEntity personneEntity = this.personneRepository.findById(id).orElse(null);
        if(personneEntity!=null) {
            return this.personneMapper.fromEntityToDto(personneEntity);
        }
        return null;
    }
}
