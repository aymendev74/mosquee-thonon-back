package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.mosqueethonon.bean.EleveBean;
import org.mosqueethonon.bean.GroupeElevesBean;
import org.mosqueethonon.entity.ClasseEntity;
import org.mosqueethonon.entity.EleveEntity;
import org.mosqueethonon.entity.InscriptionEntity;
import org.mosqueethonon.entity.LienClasseEleveEntity;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.repository.ClasseRepository;
import org.mosqueethonon.repository.EleveRepository;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.service.IClasseService;
import org.mosqueethonon.v1.criterias.CreateClasseCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@NoArgsConstructor
public class ClasseServiceImpl implements IClasseService {

    private InscriptionRepository inscriptionRepository;
    private ClasseRepository classeRepository;
    private EleveRepository eleveRepository;

    @Override
    public void createClasses(CreateClasseCriteria criteria) {
        List<InscriptionEntity> inscriptionsValidees = this.inscriptionRepository.getInscriptionsValideesByIdPeriode(criteria.getIdPeriode());
        if(!CollectionUtils.isEmpty(inscriptionsValidees)) {
            List<EleveBean> eleveBeans = this.mapToEleveBeans(inscriptionsValidees);
            List<GroupeElevesBean> groupeEleves = new ArrayList<>();
            // On construit les groupes d'élèves en fonction des niveaux à regrouper ensemble
            for(List<NiveauInterneEnum> niveaux : criteria.getGroupesNiveaux()) {
                // On identifie les élèves qui font partie des niveaux à regrouper
                List<EleveBean> elevesNiveaux = eleveBeans.stream().filter(eleve -> niveaux.contains(eleve.getNiveauInterne()))
                        .collect(Collectors.toList());
                // On créé le groupe d'élève
                groupeEleves.add(GroupeElevesBean.builder().eleveBeans(elevesNiveaux).niveaux(niveaux).build());
            }
            if(!CollectionUtils.isEmpty(groupeEleves)) {
                // On créé et sauvegarde les entités correspondants à ces groupes d'élèves (classes)
                groupeEleves.stream().forEach(this::createClasse);
            }
        }
    }

    private void createClasse(GroupeElevesBean groupeElevesBean) {
        ClasseEntity classe = new ClasseEntity();
        classe.setNumero(getNumeroClasse(groupeElevesBean.getNiveaux()));
        classe.setLiensClasseEleves(this.createLienClasseEleves(groupeElevesBean.getEleveBeans()));
        this.classeRepository.save(classe);
    }

    private List<LienClasseEleveEntity> createLienClasseEleves(List<EleveBean> eleveBeans) {
        List<LienClasseEleveEntity> lienClasseEleveEntities = new ArrayList<>();
        for(EleveBean eleveBean : eleveBeans) {
            LienClasseEleveEntity lienClasseEleveEntity = new LienClasseEleveEntity();
            Optional<EleveEntity> eleve = this.eleveRepository.findById(eleveBean.getIdEleve());
            if(!eleve.isPresent()) {
                throw new IllegalArgumentException("L'élève est introuvable, idElev = " + eleveBean.getIdEleve());
            }
            lienClasseEleveEntity.setEleve(eleve.get());
            lienClasseEleveEntities.add(lienClasseEleveEntity);
        }
        return lienClasseEleveEntities;
    }

    private String getNumeroClasse(List<NiveauInterneEnum> niveaux) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < niveaux.size(); i++) {
            sb.append(niveaux.get(i).toString());
            if(i < niveaux.size() -1) {
                sb.append(" + ");
            }
        }
        return sb.toString();
    }

    private List<EleveBean> mapToEleveBeans(List<InscriptionEntity> inscriptions) {
        List<EleveBean> eleveBeans = new ArrayList<>();
        for(InscriptionEntity inscription : inscriptions) {
            eleveBeans.addAll(inscription.getEleves().stream().map(eleve -> EleveBean.builder().idInscription(inscription.getId())
                    .idEleve(eleve.getId()).niveauInterne(eleve.getNiveauInterne()).build())
                    .collect(Collectors.toList()));

        }
        return eleveBeans;
    }
}
