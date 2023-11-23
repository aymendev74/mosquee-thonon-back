package org.mosqueethonon.entity;

import lombok.Data;
import org.mosqueethonon.v1.enums.StatutInscription;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "resplegal", schema = "moth")
@Data
public class ResponsableLegalEntity implements Auditable {

    @Id
    @Column(name = "idresp")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "txrespnom")
    private String nom;
    @Column(name = "txrespprenom")
    private String prenom;
    @Column(name = "txrespphone")
    private String telephone;
    @Column(name = "txrespmobile")
    private String mobile;
    @Column(name = "txrespemail")
    private String email;
    @Column(name = "txrespnumrue")
    private String numeroEtRue;
    @Column(name = "norespcodepostal")
    private Integer codePostal;
    @Column(name = "txrespville")
    private String ville;
    @Embedded
    private Signature signature;

}
