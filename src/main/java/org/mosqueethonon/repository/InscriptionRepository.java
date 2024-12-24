package org.mosqueethonon.repository;

import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.entity.inscription.InscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface InscriptionRepository extends JpaRepository<InscriptionEntity, Long>, JpaSpecificationExecutor<InscriptionEntity> {

    @Query(value = "select nextval('moth.inscription_noinscinscription_seq')", nativeQuery = true)
    Long getNextNumeroInscription();

    @Query(value = "select count(*) from moth.inscription i "
            + "inner join moth.eleve e on e.idinsc = i.idinsc "
            + "inner join moth.tarif t on t.idtari = e.idtari "
            + "inner join moth.periode p on p.idperi = t.idperi "
            + "where p.idperi = :idPeriode "
            + "and i.cdinsctype = :type "
            + "and i.cdinscstatut IN ('VALIDEE', 'PROVISOIRE')", nativeQuery = true)
    Integer getNbElevesInscritsByIdPeriode(@Param("idPeriode") Long idPeriode, @Param("type") String type);


    @Query(value = "select count(*) from moth.inscription i "
            + "inner join moth.eleve e on e.idinsc = i.idinsc "
            + "inner join moth.tarif t on t.idtari = e.idtari "
            + "inner join moth.periode p on p.idperi = t.idperi "
            + "where p.idperi = :idPeriode "
            + "and i.cdinsctype = :type "
            + "and not (i.dtinscinscription between :dateDebut and :dateFin)", nativeQuery = true)
    Integer getNbInscriptionOutsideRange(@Param("idPeriode") Long idPeriode, @Param("dateDebut")  LocalDate dateDebut,
                                         @Param("dateFin") LocalDate dateFin,  @Param("type") String type);

    @Query("SELECT e " +
            "FROM InscriptionEntity i " +
            "JOIN i.eleves e " +
            "JOIN TarifEntity t on t.id = e.idTarif " +
            "JOIN t.periode p " +
            "WHERE i.statut = 'VALIDEE' " +
            "AND UPPER(e.nom) = UPPER(:nom) " +
            "AND UPPER(e.prenom) = UPPER(:prenom) " +
            "AND e.dateNaissance = :dateNaissance " +
            "AND p.id = :idPeriode")
    EleveEntity findFirstEleveByNomPrenomDateNaissanceIdPeriode(@Param("nom") String nom, @Param("prenom") String prenom, @Param("dateNaissance") LocalDate dateNaissance,
                                              @Param("idPeriode") Long idPeriode);

}
