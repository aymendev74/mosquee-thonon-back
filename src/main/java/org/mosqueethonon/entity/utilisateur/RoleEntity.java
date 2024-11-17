package org.mosqueethonon.entity.utilisateur;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "role", schema = "moth")
@Data
@Immutable
public class RoleEntity {

    @Id
    @Column(name = "idrole")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cdrole")
    private String role;

}
