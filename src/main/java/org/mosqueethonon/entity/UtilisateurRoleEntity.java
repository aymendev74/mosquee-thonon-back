package org.mosqueethonon.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "utilisateur_roles", schema = "moth")
@Data
public class UtilisateurRoleEntity {

    @Id
    @Column(name = "idutro")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cdutrorole")
    private String role;

}
