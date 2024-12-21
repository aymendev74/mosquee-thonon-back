package org.mosqueethonon.service.impl.inscription;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.classe.ClasseEntity;
import org.mosqueethonon.entity.classe.LienClasseEleveEntity;
import org.mosqueethonon.entity.inscription.EleveEnrichedEntity;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.enums.AffectationEleveEnum;
import org.mosqueethonon.enums.ResultatEnum;
import org.mosqueethonon.exception.BadRequestException;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.ClasseRepository;
import org.mosqueethonon.repository.EleveEnrichedRepository;
import org.mosqueethonon.repository.EleveRepository;
import org.mosqueethonon.service.inscription.EleveService;
import org.mosqueethonon.v1.criterias.SearchEleveCriteria;
import org.mosqueethonon.v1.dto.inscription.EleveDto;
import org.mosqueethonon.v1.dto.inscription.EleveEnrichedDto;
import org.mosqueethonon.v1.mapper.inscription.EleveEnrichedMapper;
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
    private EleveEnrichedMapper eleveEnrichedMapper;
    private EleveEnrichedRepository eleveEnrichedRepository;

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

    @Override
    public void patchEleves(JsonNode patchesNode) {
        if(patchesNode.has("eleves") && patchesNode.get("eleves").elements().hasNext()) {
            patchesNode.get("eleves").forEach(this::patchEleve);
        } else {
            throw new BadRequestException("Missing non empty 'eleves' field to patch eleves !");
        }
    }

    private void patchEleve(JsonNode patchNode) {
        if (!patchNode.has("id") || !patchNode.get("id").isNumber()) {
            throw new BadRequestException("Missing 'id' field or wrong type (expect Long) to patch eleve !");
        }
        Long id = patchNode.get("id").asLong();
        EleveEntity eleve = this.eleveRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Eleve with id " + id + " does not exist !"));
        if (patchNode.has("resultat")) {
            if (patchNode.get("resultat").isNull()) {
                eleve.setResultat(null);
            } else {
                eleve.setResultat(ResultatEnum.valueOf(patchNode.get("resultat").asText()));
            }
        }
        this.eleveRepository.save(eleve);
    }

    @Override
    public List<EleveEnrichedDto> findElevesEnrichedByIdClasse(Long idClasse) {
        List<EleveEnrichedEntity> eleves = this.eleveEnrichedRepository.findByIdClasse(idClasse);
        return eleves.stream().map(eleveEnrichedMapper::fromEntityToDto).collect(Collectors.toList());
    }

}