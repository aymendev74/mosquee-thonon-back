package org.mosqueethonon.entity.document;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;
import org.mosqueethonon.enums.DocumentRequestStatut;
import org.mosqueethonon.enums.DocumentRequestType;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "document_request", schema = "moth")
@Getter
@Setter
public class DocumentRequestEntity implements Auditable {

    @Id
    @Column(name = "iddore")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cddoretype")
    @Enumerated(EnumType.STRING)
    private DocumentRequestType type;

    @Column(name = "iddorebusi")
    private Long businessId;

    @Column(name = "cddorestatut")
    @Enumerated(EnumType.STRING)
    private DocumentRequestStatut statut;

    @Column(name = "tddorecode")
    private String documentCode;

    @Column(name = "tddorechemin")
    private String documentPath;

    @Column(name = "tddoreerror")
    private String errorMessage;

    @Embedded
    private Signature signature;

    @Version
    @Column(name = "oh_version")
    private Long version;

}
