package org.mosqueethonon.entity.utilisateur;

import jakarta.persistence.*;
import lombok.Data;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;

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

}
