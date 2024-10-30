package org.mosqueethonon.repository;

import org.mosqueethonon.entity.inscription.EleveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EleveRepository extends JpaRepository<EleveEntity, Long> {

    @Query("SELECT e FROM EleveEntity e "
            + "join InscriptionEntity i on i.id = e.idInscription "
            + "join TarifEntity t on t.id = e.idTarif "
            + "join t.periode p"
            + "where p.anneeDebut = :anneeDebut "
            + "and p.anneeFin = :anneeFin "
            + "and i.type = 'ENFANT' "
            + "and i.statut = 'VALIDEE' "
            + "and e.niveauInterne != null")
    List<EleveEntity> findElevesEnfantByAnneeScolaire(@Param("anneeDebut") Integer anneeDebut, @Param("anneeFin") Integer anneeFin);

}
