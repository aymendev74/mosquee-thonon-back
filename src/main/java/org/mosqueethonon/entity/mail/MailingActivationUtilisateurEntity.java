package org.mosqueethonon.entity.mail;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;
import org.mosqueethonon.enums.MailRequestStatut;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "utilisateur_activation_mailing", schema = "moth")
@Getter
@Setter
public class MailingActivationUtilisateurEntity implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iduama")
    private Long id;

    @Column(name = "txuamauser")
    private String username;

    @Column(name = "txuamatoken")
    private String token;

    @Column(name = "cduamastatut")
    @Enumerated(EnumType.STRING)
    private MailRequestStatut statut;

    private Signature signature;

}
