package org.mosqueethonon.entity;

import lombok.Data;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.NiveauScolaireEnum;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "eleve", schema = "moth")
@Data
public class EleveEntity implements Auditable{

    @Id
    @Column(name = "idelev")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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
    @Embedded
    private Signature signature;

}
