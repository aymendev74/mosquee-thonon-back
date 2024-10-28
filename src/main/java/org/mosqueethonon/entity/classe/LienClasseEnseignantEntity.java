package org.mosqueethonon.entity.classe;

import jakarta.persistence.*;
import lombok.Data;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "lien_classe_enseignant", schema = "moth")
@Data
public class LienClasseEnseignantEntity implements Auditable {

    @Id
    @Column(name = "idlice")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "idense")
    private EnseignantEntity enseignant;
    @Embedded
    private Signature signature;

}
