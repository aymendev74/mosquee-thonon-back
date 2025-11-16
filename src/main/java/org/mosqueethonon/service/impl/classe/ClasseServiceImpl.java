package org.mosqueethonon.service.impl.classe;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.mosqueethonon.bean.GroupeElevesBean;
import org.mosqueethonon.configuration.security.context.SecurityContext;
import org.mosqueethonon.entity.classe.*;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.enums.JourActiviteEnum;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.ClasseRepository;
import org.mosqueethonon.repository.EleveRepository;
import org.mosqueethonon.service.classe.IClasseService;
import org.mosqueethonon.v1.criterias.CreateClasseCriteria;
import org.mosqueethonon.v1.criterias.SearchClasseCriteria;
import org.mosqueethonon.v1.dto.classe.ClasseDto;
import org.mosqueethonon.v1.mapper.classe.ClasseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
@NoArgsConstructor
public class ClasseServiceImpl implements IClasseService {

    private ClasseRepository classeRepository;
    private EleveRepository eleveRepository;
    private ClasseMapper classeMapper;
    private static final Set<JourActiviteEnum> JOURS_CLASSE = Sets.newHashSet(JourActiviteEnum.values());
    private SecurityContext securityContext;

    @Override
    @Transactional
    public void createClasses(CreateClasseCriteria criteria) {
        // Récupérer tous les élèves inscrits durant la période scolaire
        List<EleveEntity> eleves = this.eleveRepository.findElevesEnfantByAnneeScolaire(criteria.getDebutAnneeScolaire(), criteria.getFinAnneeScolaire(), true);
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

    @Override
    @Transactional
    public ClasseDto createClasse(ClasseDto classe) {
        ClasseEntity classeEntity = this.classeMapper.fromDtoToEntity(classe);
        this.syncNiveauEleves(classeEntity);
        classeEntity = this.classeRepository.save(classeEntity);
        // Les élèves qui n'ont pas niveaux vont se voir attribuer le niveau de la classe
        this.syncOtherClasses(classeEntity);
        return this.classeMapper.fromEntityToDto(classeEntity);
    }

    @Override
    @Transactional
    public ClasseDto updateClasse(Long id, ClasseDto classe) {
        ClasseEntity classeEntity = this.classeRepository.findById(id).orElse(null);
        if(classeEntity == null) {
            throw new ResourceNotFoundException("La classe n'a pas été trouvée, id = " + id);
        }
        this.classeMapper.updateClasseEntity(classe, classeEntity);
        this.syncNiveauEleves(classeEntity);
        this.checkFeuillesPresence(classeEntity);
        classeEntity = this.classeRepository.save(classeEntity);
        this.syncOtherClasses(classeEntity);
        return this.classeMapper.fromEntityToDto(classeEntity);
    }

    /**
     * Vérifie les feuilles de présences afin de s'assurer que les èléves qui sont dans les feuilles
     * de présence font bien partie de l'effectif de la classe, sinon on les supprime des feuilles de présence.
     * Et ci un élève est dans l'effectif de la classe mais qu'il ne figure pas dans les feuilles de présences (ajout d'un nouvel élève)
     * alors on l'ajoute dans les feuilles de présences (absent par défaut)
     * Peut se produire si on modifie l'effectif d'une classe après que des élèves ont été ajoutés à des feuilles de présence.
     * @param classeEntity
     */
    private void checkFeuillesPresence(ClasseEntity classeEntity) {
        var idEleves = classeEntity.getLiensClasseEleves().stream().map(LienClasseEleveEntity::getEleve)
                .map(EleveEntity::getId).collect(Collectors.toSet());

        // on supprime des feuilles de présence, les élèves qui ne font plus partie de la classe
        var feuillesPresences = classeEntity.getFeuillesPresences().stream().map(ClasseFeuillePresenceEntity::getFeuillePresence).collect(Collectors.toSet());
        feuillesPresences.stream().map(FeuillePresenceEntity::getElevesFeuillesPresences).forEach(elevesFeuillePresence ->
                        elevesFeuillePresence.removeIf(eleveFeuille -> !idEleves.contains(eleveFeuille.getIdEleve())));

        // On ajoute une absence aux élèves qui sont dans l'effectif de la classe mais qui ne figurent pas dans les feuilles de présences
        for(FeuillePresenceEntity feuillePresence : feuillesPresences) {
            var idElevesFeuillePresence = feuillePresence.getElevesFeuillesPresences().stream().map(EleveFeuillePresenceEntity::getIdEleve).collect(Collectors.toSet());
            for(Long idEleveClasse : idEleves) {
                if(!idElevesFeuillePresence.contains(idEleveClasse)) {
                    var eleveFeuillePresence = EleveFeuillePresenceEntity.builder().idEleve(idEleveClasse).present(false).build();
                    feuillePresence.getElevesFeuillesPresences().add(eleveFeuillePresence);
                }
            }
        }
    }

    /**
     * Synchronise l'effectif des autres classes, de manière à ce que tout élève ne figure que dans une seule classe
     * @param classeEntity la classe en cours de sauvegarde et dont l'effectif est potentiellement modifié
     */
    private void syncOtherClasses(ClasseEntity classeEntity) {
        // On récupère toutes les autres classes de la même période
        List<ClasseEntity> allOtherClasses = this.classeRepository.findByDebutAnneeScolaireAndFinAnneeScolaire(classeEntity.getDebutAnneeScolaire(),
                classeEntity.getFinAnneeScolaire()).stream().filter(classe -> !classe.getId().equals(classeEntity.getId())).toList();
        Set<Long> idEleves = classeEntity.getLiensClasseEleves().stream().map(LienClasseEleveEntity::getEleve).map(EleveEntity::getId).collect(Collectors.toSet());
        // On ne garde dans chacune de ces classes, que les élèves qui ne sont pas dans la classe en cours de sauvegarde
        for(ClasseEntity otherClasse : allOtherClasses) {
            boolean removed = otherClasse.getLiensClasseEleves().removeIf(lienClasseEleve -> idEleves.contains(lienClasseEleve.getEleve().getId()));
            if(removed) {
                this.classeRepository.save(otherClasse);
            }
        }
    }

    private void syncNiveauEleves(ClasseEntity classeEntity) {
        for(LienClasseEleveEntity lienClasseEleve : classeEntity.getLiensClasseEleves()) {
            EleveEntity eleve = this.eleveRepository.findById(lienClasseEleve.getEleve().getId()).orElseThrow(
                    () -> new ResourceNotFoundException("L'eleve n'a pas été trouvée, id = " + lienClasseEleve.getEleve().getId()));
            if(eleve.getNiveauInterne() == null) {
                eleve.setNiveauInterne(classeEntity.getNiveau());
                this.eleveRepository.save(eleve);
            }
        }
    }

    @Override
    public List<ClasseDto> findClassesByCriteria(SearchClasseCriteria criteria) {
        List<ClasseEntity> classes = null;
        if(this.securityContext.isAdmin()) {
            classes = this.classeRepository.findByDebutAnneeScolaireAndFinAnneeScolaire(criteria.getAnneeDebut(), criteria.getAnneeFin());
        } else {
            String username = this.securityContext.getVisa();
            classes = this.classeRepository.findByDebutAnneeScolaireAndFinAnneeScolaireAndEnseignantUsername(criteria.getAnneeDebut(), criteria.getAnneeFin(), username);
        }
        return classes.stream().map(this.classeMapper::fromEntityToDto).collect(Collectors.toList());
    }

    @Override
    public void deleteClasse(Long id) {
        this.classeRepository.deleteById(id);
    }

    @Override
    public ClasseDto findClasseById(Long id) {
        ClasseEntity classe = this.classeRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("La classe n'a pas été trouvée, id = " + id)
        );
        return this.classeMapper.fromEntityToDto(classe);
    }
}
