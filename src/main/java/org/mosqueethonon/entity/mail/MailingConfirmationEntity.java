package org.mosqueethonon.entity.mail;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;
import org.mosqueethonon.enums.MailingConfirmationStatut;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "mailingconfirmation", schema = "moth")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailingConfirmationEntity implements Auditable {

    @Id
    @Column(name = "idmaco")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cdmacostatut")
    @Enumerated(EnumType.STRING)
    private MailingConfirmationStatut statut;
    @Column(name = "idinsc")
    private Long idInscription;
    @Column(name = "idadhe")
    private Long idAdhesion;
    @Embedded
    private Signature signature;

}
