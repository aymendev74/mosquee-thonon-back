package org.mosqueethonon.utilisateur.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.mosqueethonon.common.audit.Auditable;
import org.mosqueethonon.common.audit.EntityListener;
import org.mosqueethonon.common.audit.Signature;
import org.mosqueethonon.mail.enums.MailRequestStatutEnum;
import org.mosqueethonon.utilisateur.enums.UserAccountActionTypeEnum;

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
    private MailRequestStatutEnum statut;

    @Column(name = "cduaactype")
    @Enumerated(EnumType.STRING)
    private UserAccountActionTypeEnum type;

    @Embedded
    private Signature signature;
    @Version
    @Column(name = "oh_version")
    private Long version;

}
