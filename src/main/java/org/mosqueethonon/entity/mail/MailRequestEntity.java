package org.mosqueethonon.entity.mail;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.mosqueethonon.dto.mail.MailAttachmentDto;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;
import org.mosqueethonon.enums.MailRequestStatut;
import org.mosqueethonon.enums.MailRequestType;

import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "mail_request", schema = "moth")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailRequestEntity implements Auditable {

    @Id
    @Column(name = "idmare")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "cdmaretype", nullable = false, length = 50)
    private MailRequestType type;

    @Column(name = "businessid", nullable = false)
    private Long businessId;

    @Column(name = "cdmarestatut", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private MailRequestStatut statut;

    @Column(name = "txmaresubject")
    private String subject;

    @Column(name = "txmarebody")
    private String body;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "txmareattachments")
    private List<MailAttachmentDto> attachments;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idmare", nullable = false)
    @Builder.Default
    private List<MailRequestDocumentRequestEntity> documentRequests = new ArrayList<>();

    @Embedded
    private Signature signature;

    @Version
    @Column(name = "oh_version")
    private Long version;

}
