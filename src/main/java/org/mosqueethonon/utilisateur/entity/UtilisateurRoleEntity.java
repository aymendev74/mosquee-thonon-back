package org.mosqueethonon.utilisateur.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.mosqueethonon.common.audit.Auditable;
import org.mosqueethonon.common.audit.EntityListener;
import org.mosqueethonon.common.audit.Signature;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "utilisateur_roles", schema = "moth")
@Data
public class UtilisateurRoleEntity implements Auditable {

    @Id
    @Column(name = "idutro")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cdutrorole")
    private String role;
    @Embedded
    private Signature signature;
    @Version
    @Column(name = "oh_version")
    private Long version;

}
