package org.mosqueethonon.entity.classe;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@AllArgsConstructor
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
    @Column(name = "idense")
    private Long idEnseignant;
    @ManyToOne
    @JoinColumn(name = "idense", insertable = false, updatable = false)
    private EnseignantEntity enseignant;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idclas", nullable = false)
    private List<LienClasseEleveEntity> liensClasseEleves;
    @Column(name = "noclasanneedebut")
    private Integer debutAnneeScolaire;
    @Column(name = "noclasanneefin")
    private Integer finAnneeScolaire;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idclas", nullable = false)
    private List<ClasseActiviteEntity> activites;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idclas", insertable = false, updatable = false)
    private List<ClasseFeuillePresenceEntity> feuillesPresences;
    @Embedded
    private Signature signature;

}
