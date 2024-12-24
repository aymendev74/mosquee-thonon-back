package org.mosqueethonon.entity.inscription;

import lombok.Data;
import org.hibernate.annotations.Formula;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.NiveauScolaireEnum;

import jakarta.persistence.*;
import org.mosqueethonon.enums.ResultatEnum;
import org.mosqueethonon.enums.SexeEnum;

import java.time.LocalDate;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "eleve", schema = "moth")
@Data
public class EleveEntity implements Auditable {

    @Id
    @Column(name = "idelev")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "idinsc", insertable = false, updatable = false)
    private Long idInscription;
    @Column(name = "txelevnom")
    private String nom;
    @Column(name = "txelevprenom")
    private String prenom;
    @Column(name = "dtelevnaissance")
    private LocalDate dateNaissance;
    @Column(name = "cdelevniveau")
    @Enumerated(EnumType.STRING)
    private NiveauScolaireEnum niveau;
    @Column(name = "cdelevniveauinterne")
    @Enumerated(EnumType.STRING)
    private NiveauInterneEnum niveauInterne;
    @Column(name = "idtari")
    private Long idTarif;
    @ManyToOne
    @JoinColumn(name = "idtari", insertable = false, updatable = false)
    private TarifEntity tarif;
    @Column(name = "cdelevsexe")
    @Enumerated(EnumType.STRING)
    private SexeEnum sexe;
    @Column(name = "cdelevresultat")
    @Enumerated(EnumType.STRING)
    private ResultatEnum resultat;
    @Formula("(select lien.idclas from moth.lien_classe_eleve lien where lien.idelev = idelev limit 1)")
    private Long classeId;
    @Embedded
    private Signature signature;

}
