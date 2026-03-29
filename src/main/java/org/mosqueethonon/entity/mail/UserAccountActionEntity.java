package org.mosqueethonon.entity.mail;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;
import org.mosqueethonon.enums.MailRequestStatut;
import org.mosqueethonon.enums.UserAccountActionType;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "utilisateur_account_action", schema = "moth")
@Getter
@Setter
public class UserAccountActionEntity implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iduaac")
    private Long id;

    @Column(name = "txuaacuser")
    private String username;

    @Column(name = "txuaactoken")
    private String token;

    @Column(name = "cduaacstatut")
    @Enumerated(EnumType.STRING)
    private MailRequestStatut statut;

    @Column(name = "cduaactype")
    @Enumerated(EnumType.STRING)
    private UserAccountActionType type;

    @Embedded
    private Signature signature;
    @Version
    @Column(name = "oh_version")
    private Long version;

}
