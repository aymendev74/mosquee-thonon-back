package org.mosqueethonon.entity.mail;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.mosqueethonon.dto.mail.MailAttachmentDto;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;
import org.mosqueethonon.enums.MailRequestStatut;
import org.mosqueethonon.enums.MailRequestType;

import java.util.List;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "mail_request", schema = "moth")
@Data
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

    @Embedded
    private Signature signature;

}
