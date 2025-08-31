package org.mosqueethonon.entity.bulletin;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.Signature;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "bulletin", schema = "moth")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulletinEntity implements Auditable {

    @Id
    @Column(name = "idbull")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "idelev")
    private Long idEleve;
    @Column(name = "cdbullappreciation")
    private String appreciation;
    @Column(name = "nbbullabsences")
    private Integer nbAbsences;
    @Column(name = "nobullmois")
    private Integer mois;
    @Column(name = "nobullannee")
    private Integer annee;
    @Column(name = "dtbullbulletin")
    private LocalDate dateBulletin;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idbull", nullable = false)
    private List<BulletinMatiereEntity> bulletinMatieres;
    @Embedded
    private Signature signature;

}
