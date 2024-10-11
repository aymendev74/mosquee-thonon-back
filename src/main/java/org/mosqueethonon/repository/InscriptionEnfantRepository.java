package org.mosqueethonon.repository;

import org.mosqueethonon.entity.inscription.InscriptionEnfantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InscriptionEnfantRepository extends JpaRepository<InscriptionEnfantEntity, Long>, JpaSpecificationExecutor<InscriptionEnfantEntity> {

    @Query("select i from InscriptionEnfantEntity i "
            + "join i.responsableLegal r "
            + "join TarifEntity t on t.id = r.idTarif "
            + "join t.periode p "
            + "where p.id = :idPeriode "
            + "and i.statut in ('LISTE_ATTENTE') "
            + "and i.type = 'ENFANT' "
            + "order by i.noPositionAttente")
    List<InscriptionEnfantEntity> getInscriptionEnAttenteByPeriode(@Param("idPeriode") Long idPeriode);

    @Query("select distinct i from InscriptionEnfantEntity i "
            + "join i.eleves e "
            + "join TarifEntity t on t.id = e.idTarif "
            + "join t.periode p "
            + "where :atDate between p.dateDebut and p.dateFin "
            + "and i.statut in ('VALIDEE', 'PROVISOIRE', 'LISTE_ATTENTE') "
            + "and e.prenom = :prenom and e.nom = :nom "
            + "and i.type = 'ENFANT' "
            + "and i.id <> coalesce(:excludedInscription, -1)")
    List<InscriptionEnfantEntity> findInscriptionsWithEleve(String prenom, String nom, LocalDate atDate, Long excludedInscription);


    @Query(value = "select max(noinscpositionattente) from moth.inscription i "
            + "inner join moth.resplegal e on e.idresp = i.idresp "
            + "inner join moth.tarif t on t.idtari = e.idtari "
            + "inner join moth.periode p on p.idperi = t.idperi "
            + "where p.dtperidebut <= :atDate and p.dtperifin >= :atDate "
            + "and i.cdinsctype = 'ENFANT' "
            + "and i.cdinscstatut = 'LISTE_ATTENTE' ", nativeQuery = true)
    Integer getLastPositionAttente(@Param("atDate") LocalDate atDate);

    @Query(value = "select max(noinscpositionattente) from moth.inscription i "
            + "inner join moth.resplegal e on e.idresp = i.idresp "
            + "inner join moth.tarif t on t.idtari = e.idtari "
            + "inner join moth.periode p on p.idperi = t.idperi "
            + "where p.idperi = :idPeriode "
            + "and i.cdinsctype = 'ENFANT' "
            + "and i.cdinscstatut = 'LISTE_ATTENTE' ", nativeQuery = true)
    Integer getLastPositionAttente(@Param("idPeriode") Long idPeriode);
}
