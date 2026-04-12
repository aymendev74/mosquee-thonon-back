package org.mosqueethonon.entity.document;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "document", schema = "moth")
@Getter
@Setter
public class DocumentEntity implements Auditable {

    @Id
    @Column(name = "iddocu")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "txdoculibelle")
    private String libelle;

    @Column(name = "cddocutype")
    private String code;

    @Column(name = "txdocuchemin")
    private String chemin;

    @Column(name = "txdocuhash")
    private String hash;

    @Column(name = "txdocuannee")
    private String annee;

    @Column(name = "idutil")
    private Long idUtilisateur;

    @Embedded
    private Signature signature;

    @Version
    @Column(name = "oh_version")
    private Long version;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "iddocu", nullable = false)
    private List<DocumentMetadataEntity> metadonnees = new ArrayList<>();

}
