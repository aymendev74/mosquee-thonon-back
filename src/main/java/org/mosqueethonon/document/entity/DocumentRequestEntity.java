package org.mosqueethonon.document.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.mosqueethonon.common.audit.Auditable;
import org.mosqueethonon.common.audit.EntityListener;
import org.mosqueethonon.common.audit.Signature;
import org.mosqueethonon.document.enums.DocumentRequestStatutEnum;
import org.mosqueethonon.document.enums.DocumentRequestTypeEnum;

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
    private DocumentRequestTypeEnum type;

    @Column(name = "iddorebusi")
    private Long businessId;

    @Column(name = "cddorestatut")
    @Enumerated(EnumType.STRING)
    private DocumentRequestStatutEnum statut;

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
