package org.mosqueethonon.repository;

import org.mosqueethonon.entity.classe.ClasseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClasseRepository extends JpaRepository<ClasseEntity, Long> {

    List<ClasseEntity> findByDebutAnneeScolaireAndFinAnneeScolaire(Integer debutAnneeScolaire, Integer finAnneeScolaire);

    @Query("SELECT DISTINCT c FROM ClasseEntity c JOIN c.liensClasseEnseignants e WHERE c.debutAnneeScolaire = :debutAnneeScolaire AND c.finAnneeScolaire = :finAnneeScolaire AND e.enseignant.username = :username")
    List<ClasseEntity> findByDebutAnneeScolaireAndFinAnneeScolaireAndEnseignantUsername(@Param("debutAnneeScolaire") Integer debutAnneeScolaire, @Param("finAnneeScolaire") Integer finAnneeScolaire, @Param("username") String username);

}
