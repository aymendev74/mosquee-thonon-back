package org.mosqueethonon.entity.classe;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;
import org.mosqueethonon.enums.NiveauInterneEnum;

import java.util.List;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "classe", schema = "moth")
@Builder
@Data
public class ClasseEntity implements Auditable {

    @Id
    @Column(name = "idclas")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "txclaslibelle")
    private String libelle;
    @Column(name = "cdclasniveau")
    @Enumerated(EnumType.STRING)
    private NiveauInterneEnum niveau;
    @ManyToOne
    @JoinColumn(name = "idense", nullable = false)
    private EnseignantEntity enseignant;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "idclas", nullable = false)
    private List<LienClasseEleveEntity> liensClasseEleves;
    @Column(name = "noclasanneedebut")
    private Integer debutAnneeScolaire;
    @Column(name = "noclasanneefin")
    private Integer finAnneeScolaire;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "idclas", nullable = false)
    private List<ClasseActiviteEntity> activites;
    @Embedded
    private Signature signature;

}