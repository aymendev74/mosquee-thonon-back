package org.mosqueethonon.entity.classe;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;
import org.mosqueethonon.entity.inscription.EleveEntity;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "lien_classe_eleve", schema = "moth")
@Data
@Builder
public class LienClasseEleveEntity implements Auditable {

    @Id
    @Column(name = "idlcel")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "idelev")
    private EleveEntity eleve;
    @Embedded
    private Signature signature;

}
