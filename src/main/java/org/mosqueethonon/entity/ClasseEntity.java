package org.mosqueethonon.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "classe", schema = "moth")
@Data
public class ClasseEntity implements Auditable {

    @Id
    @Column(name = "idclas")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "noclasnum")
    private String numero;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "idclas")
    private List<LienClasseEnseignantEntity> lienClasseEnseignantEntities;
    @Embedded
    private Signature signature;

}
