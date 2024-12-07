package org.mosqueethonon.service;

import org.mosqueethonon.v1.criterias.CreateClasseCriteria;
import org.mosqueethonon.v1.criterias.SearchClasseCriteria;
import org.mosqueethonon.v1.dto.classe.ClasseDto;

import java.util.List;

public interface IClasseService {

    void createClasses(CreateClasseCriteria criteria);

    ClasseDto createClasse(ClasseDto classe);

    ClasseDto updateClasse(Long id, ClasseDto classe);

    List<ClasseDto> findClassesByCriteria(SearchClasseCriteria criteria);

    void deleteClasse(Long id);
}
