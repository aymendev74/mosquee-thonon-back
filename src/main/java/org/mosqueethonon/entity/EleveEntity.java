package org.mosqueethonon.entity;

import lombok.Data;

import javax.persistence.*;
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
    private String niveau;
    @Embedded
    private Signature signature;

}
