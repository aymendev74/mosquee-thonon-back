package org.mosqueethonon.classe.service;

import org.mosqueethonon.classe.v1.criteria.CreateClasseCriteria;
import org.mosqueethonon.classe.v1.criteria.SearchClasseCriteria;
import org.mosqueethonon.classe.v1.dto.ClasseDto;

import java.util.List;

public interface ClasseService {

    void createClasses(CreateClasseCriteria criteria);

    ClasseDto createClasse(ClasseDto classe);

    ClasseDto updateClasse(Long id, ClasseDto classe);

    List<ClasseDto> findClassesByCriteria(SearchClasseCriteria criteria);

    void deleteClasse(Long id);

    ClasseDto findClasseById(Long id);

}
