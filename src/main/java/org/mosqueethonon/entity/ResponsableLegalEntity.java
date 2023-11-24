package org.mosqueethonon.entity;

import lombok.Data;
import javax.persistence.*;

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
    @Column(name = "idtari")
    private Long idTarif;
    @Column(name = "lorespadherent")
    private Boolean adherent;
    @Embedded
    private Signature signature;

}
