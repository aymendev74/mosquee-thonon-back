package org.mosqueethonon.service.impl.inscription;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.classe.ClasseEntity;
import org.mosqueethonon.entity.classe.LienClasseEleveEntity;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.enums.AffectationEleveEnum;
import org.mosqueethonon.repository.ClasseRepository;
import org.mosqueethonon.repository.EleveRepository;
import org.mosqueethonon.service.inscription.EleveService;
import org.mosqueethonon.v1.criterias.SearchEleveCriteria;
import org.mosqueethonon.v1.dto.inscription.EleveDto;
import org.mosqueethonon.v1.mapper.inscription.EleveMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EleveServiceImpl implements EleveService {

    private ClasseRepository classeRepository;
    private EleveRepository eleveRepository;
    private EleveMapper eleveMapper;

    @Override
    public List<EleveDto> findElevesByCriteria(SearchEleveCriteria criteria) {
        // On récupère tous les élèves de la période scolaire
        List<EleveEntity> eleves = this.eleveRepository.findElevesEnfantByAnneeScolaire(criteria.getAnneeDebut(), criteria.getAnneeFin());
        if (eleves.isEmpty()) {
            return List.of();
        }

        if (criteria.getAffectation() == null || criteria.getAffectation() == AffectationEleveEnum.SANS_IMPORTANCE) {
            return eleves.stream().map(eleveMapper::fromEntityToDto).collect(Collectors.toList());
        }

        List<ClasseEntity> classes = this.classeRepository.findByDebutAnneeScolaireAndFinAnneeScolaire(criteria.getAnneeDebut(), criteria.getAnneeFin());
        List<EleveDto> resultEleves = new ArrayList<>();
        for (EleveEntity eleve : eleves) {
            EleveEntity eleveClasse = classes.stream().map(ClasseEntity::getLiensClasseEleves).flatMap(List::stream)
                    .map(LienClasseEleveEntity::getEleve).filter(eleveEntity -> eleveEntity.getId().equals(eleve.getId()))
                    .findFirst().orElse(null);

            switch (criteria.getAffectation()) {
                case AVEC_AFFECTATION:
                    if (eleveClasse != null) {
                        resultEleves.add(eleveMapper.fromEntityToDto(eleve));
                    }
                    break;
                case SANS_AFFECTATION:
                    if (eleveClasse == null) {
                        resultEleves.add(eleveMapper.fromEntityToDto(eleve));
                    }
                    break;
                default:
                    throw new IllegalArgumentException("la valeur n'est pas gérée ici : " + criteria.getAffectation());
            }
        }
        return resultEleves;
    }

}