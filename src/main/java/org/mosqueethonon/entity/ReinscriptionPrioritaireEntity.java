package org.mosqueethonon.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;

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
