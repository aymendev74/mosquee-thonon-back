package org.mosqueethonon.repository;

import org.mosqueethonon.entity.InscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InscriptionRepository extends JpaRepository<InscriptionEntity, Long>, JpaSpecificationExecutor<InscriptionEntity> {

    @Query(value = "select count(*) from moth.inscription i "
            + "inner join moth.eleve e on e.idinsc = i.idinsc "
            + "inner join moth.tarif t on t.idtari = e.idtari "
            + "inner join moth.periode p on p.idperi = t.idperi "
            + "where p.idperi = :idPeriode " +
            "and i.cdinscstatut IN ('VALIDEE', 'PROVISOIRE')", nativeQuery = true)
    Integer getNbElevesInscritsByIdPeriode(Long idPeriode);

    @Query(value = "select count(*) from moth.inscription i "
            + "inner join moth.eleve e on e.idinsc = i.idinsc "
            + "inner join moth.tarif t on t.idtari = e.idtari "
            + "inner join moth.periode p on p.idperi = t.idperi "
            + "where p.idperi = :idPeriode " +
            "and not (i.dtinscinscription between :dateDebut and :dateFin)", nativeQuery = true)
    Integer getNbInscriptionOutsideRange(Long idPeriode, LocalDate dateDebut, LocalDate dateFin);

    @Query(value = "select nextval('moth.inscription_noinscinscription_seq')", nativeQuery = true)
    Long getNextNumeroInscription();

    @Query(value = "select max(noinscpositionattente) from moth.inscription i "
            + "inner join moth.resplegal e on e.idresp = i.idresp "
            + "inner join moth.tarif t on t.idtari = e.idtari "
            + "inner join moth.periode p on p.idperi = t.idperi "
            + "where p.dtperidebut <= :atDate and p.dtperifin >= :atDate " +
            "and i.cdinscstatut = 'LISTE_ATTENTE' ", nativeQuery = true)
    Integer getLastPositionAttente(LocalDate atDate);

    @Query(value = "select max(noinscpositionattente) from moth.inscription i "
            + "inner join moth.resplegal e on e.idresp = i.idresp "
            + "inner join moth.tarif t on t.idtari = e.idtari "
            + "inner join moth.periode p on p.idperi = t.idperi "
            + "where p.idperi = :idPeriode " +
            "and i.cdinscstatut = 'LISTE_ATTENTE' ", nativeQuery = true)
    Integer getLastPositionAttente(Long idPeriode);

    @Query(value = "select i.* from moth.inscription i "
            + "inner join moth.resplegal r on r.idresp = i.idresp "
            + "inner join moth.tarif t on t.idtari = r.idtari "
            + "inner join moth.periode p on p.idperi = t.idperi "
            + "where p.idperi = :idPeriode "
            + "and i.cdinscstatut IN ('LISTE_ATTENTE') "
            + "order by i.noinscpositionattente", nativeQuery = true)
    List<InscriptionEntity> getInscriptionEnAttenteByPeriode(Long idPeriode);
}
