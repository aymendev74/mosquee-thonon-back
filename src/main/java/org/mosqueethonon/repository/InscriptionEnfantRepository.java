package org.mosqueethonon.repository;

import org.mosqueethonon.entity.InscriptionEnfantEntity;
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

    @Query(value = "select i.* from moth.inscription i "
            + "inner join moth.resplegal r on r.idresp = i.idresp "
            + "inner join moth.tarif t on t.idtari = r.idtari "
            + "inner join moth.periode p on p.idperi = t.idperi "
            + "where p.idperi = :idPeriode "
            + "and i.cdinscstatut IN ('LISTE_ATTENTE') "
            + "and i.cdinsctype = 'ENFANT' "
            + "order by i.noinscpositionattente", nativeQuery = true)
    List<InscriptionEnfantEntity> getInscriptionEnAttenteByPeriode(@Param("idPeriode") Long idPeriode);

    @Query(value = "select distinct i.* from moth.inscription i "
            + "inner join moth.eleve e on e.idinsc = i.idinsc "
            + "inner join moth.tarif t on t.idtari = e.idtari "
            + "inner join moth.periode p on p.idperi = t.idperi "
            + "where :atDate between p.dtperidebut and p.dtperifin "
            + "and i.cdinscstatut IN ('VALIDEE', 'PROVISOIRE', 'LISTE_ATTENTE') "
            + "and e.txelevprenom = :prenom and e.txelevnom = :nom "
            + "and i.cdinsctype = 'ENFANT' "
            + "and i.idinsc <> coalesce(:excludedInscription, -1)", nativeQuery = true)
    List<InscriptionEnfantEntity> findInscriptionsWithEleve(String prenom, String nom, LocalDateTime atDate, Long excludedInscription);


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
