package org.mosqueethonon.entity.inscription;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "reinscriptionprioritaire", schema = "moth")
@Data
@Immutable
public class ReinscriptionPrioritaireEntity {

    @Id
    @Column(name = "idrepr")
    private Long id;
    @Column(name = "txreprnom")
    private String nom;
    @Column(name = "txreprprenom")
    private String prenom;

}
