package org.mosqueethonon.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.mosqueethonon.bean.GroupeElevesBean;
import org.mosqueethonon.entity.classe.ClasseActiviteEntity;
import org.mosqueethonon.entity.classe.ClasseEntity;
import org.mosqueethonon.entity.classe.LienClasseEleveEntity;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.enums.JourActiviteEnum;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.repository.ClasseRepository;
import org.mosqueethonon.repository.EleveRepository;
import org.mosqueethonon.service.IClasseService;
import org.mosqueethonon.v1.criterias.CreateClasseCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@NoArgsConstructor
public class ClasseServiceImpl implements IClasseService {

    private ClasseRepository classeRepository;
    private EleveRepository eleveRepository;
    private static final Set<JourActiviteEnum> JOURS_CLASSE = Sets.newHashSet(JourActiviteEnum.values());

    @Override
    public void createClasses(CreateClasseCriteria criteria) {
        // Récupérer tous les élèves inscrits durant la période scolaire
        List<EleveEntity> eleves = this.eleveRepository.findElevesEnfantByAnneeScolaire(criteria.getDebutAnneeScolaire(), criteria.getFinAnneeScolaire());
        if(eleves.isEmpty()) {
            return;
        }

        // Pour chaque élève, récupérer la classe dans laquelle il était l'année précédente pour avoir son jour d'activité
        // Crèer les groupes d'activités (samedi matin, dimanche matin, dimanche après-midi)
        // Affecter chaque élève à un groupe en fonction de son jour d'activité l'année précédente
        List<ClasseEntity> classesAnneePrecedentes = this.classeRepository.findByDebutAnneeScolaireAndFinAnneeScolaire(
                criteria.getDebutAnneeScolaire() - 1, criteria.getFinAnneeScolaire() - 1);
        List<GroupeElevesBean> groupeEleves = this.createGroupeEleves(classesAnneePrecedentes, eleves);

        // Puis créer les classes par niveau dans chacun de ces groupes
        if(!CollectionUtils.isEmpty(groupeEleves)) {
            List<ClasseEntity> classes = this.createClasses(groupeEleves, criteria.getDebutAnneeScolaire(), criteria.getFinAnneeScolaire(),
                    criteria.getNbMaxEleveParClasse());
            if(!CollectionUtils.isEmpty(classes)) {
                classes = this.classeRepository.saveAll(classes);
            }
        }
    }

    private List<ClasseEntity> createClasses(List<GroupeElevesBean> groupeEleves, Integer debutAnneeScolaire, Integer finAnneeScolaire,
                                             Integer nbMaxEleveClasse) {
        List<ClasseEntity> classes = new ArrayList<>();
        for(GroupeElevesBean groupeEleve : groupeEleves) {
            Map<NiveauInterneEnum, List<EleveEntity>> elevesByNiveau = groupeEleve.getEleves().stream().collect(Collectors.groupingBy(EleveEntity::getNiveauInterne));
            for (Map.Entry<NiveauInterneEnum, List<EleveEntity>> entrySet : elevesByNiveau.entrySet()) {
                classes.addAll(this.createClasses(entrySet.getKey(), entrySet.getValue(), debutAnneeScolaire, finAnneeScolaire, nbMaxEleveClasse,
                        groupeEleve.getJourClasse()));
            }
        }
        return classes;
    }

    private List<ClasseEntity> createClasses(NiveauInterneEnum niveau, List<EleveEntity> elevesForNiveau, Integer debutAnneeScolaire, Integer finAnneeScolaire,
                                             Integer nbMaxEleveClasse, JourActiviteEnum jourClasse) {
        List<ClasseEntity> classes = new ArrayList<>();
        // On sépare les élèves de ce niveau en plusieurs classe de nbMaxEleveClasse
        List<List<EleveEntity>> classesForNiveau = new ArrayList<>();
        for (int i = 0; i < elevesForNiveau.size(); i += nbMaxEleveClasse) {
            classesForNiveau.add(elevesForNiveau.subList(i, Math.min(i + nbMaxEleveClasse, elevesForNiveau.size())));
        }

        // Puis on créé les classes
        int nbClasse = 1;
        for(List<EleveEntity> eleves : classesForNiveau) {
            ClasseEntity classeEntity = ClasseEntity.builder()
                    .debutAnneeScolaire(debutAnneeScolaire)
                    .finAnneeScolaire(finAnneeScolaire)
                    .niveau(niveau)
                    .libelle(classesForNiveau.size() > 1 ? String.valueOf(nbClasse) : null)
                    .liensClasseEleves(eleves.stream().map(eleve -> LienClasseEleveEntity.builder().eleve(eleve).build())
                            .collect(Collectors.toList()))
                    .activites(Lists.newArrayList(ClasseActiviteEntity.builder().jour(jourClasse).build()))
                    .build();
            classes.add(classeEntity);
            nbClasse++;
        }
        return classes;
    }

    private List<GroupeElevesBean> createGroupeEleves(List<ClasseEntity> classesAnneePrecedentes, List<EleveEntity> eleves) {
        // On créé tous les groupe pour l'année en cours
        List<GroupeElevesBean> groupeEleves = JOURS_CLASSE.stream().map(jour -> GroupeElevesBean.builder().jourClasse(jour).build()).collect(Collectors.toList());
        // On créé un groupe avec le jour d'activité à null (pour les nouveaux élèves)
        groupeEleves.add(GroupeElevesBean.builder().build());

        // Pour chaque élève on va détemriner dans quel groupe le mettre
        for (EleveEntity eleve : eleves) {
            JourActiviteEnum jourActivite = this.getJourActiviteEleve(eleve, classesAnneePrecedentes);
            GroupeElevesBean groupeEleve = null;
            if(jourActivite != null)  {
                groupeEleve = groupeEleves.stream().filter(groupe -> groupe.getJourClasse() == jourActivite).findFirst().orElse(null);
                // Si le groupe avec le jour d'activité de l'an passé n'existe pas => ajout au groupe des nouveaux élèves
                if(groupeEleve == null) {
                    groupeEleve = groupeEleves.stream().filter(groupe -> groupe.getJourClasse() == null).findFirst().orElse(null);
                }
            } else {
                groupeEleve = groupeEleves.stream().filter(groupe -> groupe.getJourClasse() == null).findFirst().orElse(null);
            }
            groupeEleve.getEleves().add(eleve);
        }
        return groupeEleves;
    }

    private JourActiviteEnum getJourActiviteEleve(EleveEntity eleve, List<ClasseEntity> classesAnneePrecedentes) {
        if(eleve != null && classesAnneePrecedentes != null) {
            for(ClasseEntity classe : classesAnneePrecedentes) {
                if(CollectionUtils.isEmpty(classe.getLiensClasseEleves()) || CollectionUtils.isEmpty(classe.getActivites())) {
                    continue;
                }
                for(LienClasseEleveEntity lienClasseEleve : classe.getLiensClasseEleves()) {
                    EleveEntity ancienEleve = lienClasseEleve.getEleve();
                    if(ancienEleve.getNom().equals(eleve.getNom()) && ancienEleve.getPrenom().equals(eleve.getPrenom())
                            && ancienEleve.getDateNaissance().equals(eleve.getDateNaissance())) {
                        // En théorie les activités n'ont lieu qu'une fois par semaine par classe
                        return classe.getActivites().get(0).getJour();
                    }
                }
            }
        }
        return null;
    }

}