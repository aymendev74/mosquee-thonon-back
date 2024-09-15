package org.mosqueethonon.entity.inscription;

import lombok.Data;
import jakarta.persistence.*;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;

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
    @Column(name = "lorespautonomie")
    private Boolean autorisationAutonomie;
    @Column(name = "lorespmedia")
    private Boolean autorisationMedia;
    @Column(name = "txrespnomautre")
    private String nomAutre;
    @Column(name = "txrespprenomautre")
    private String prenomAutre;
    @Column(name = "txresplienparente")
    private String lienParente;
    @Column(name = "txrespphoneautre")
    private String telephoneAutre;
    @Embedded
    private Signature signature;

}
