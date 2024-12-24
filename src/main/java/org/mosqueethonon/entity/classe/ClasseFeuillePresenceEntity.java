package org.mosqueethonon.entity.classe;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "lien_classe_feuille_presence", schema = "moth")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClasseFeuillePresenceEntity implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idlcfp")
    private Long id;
    @Column(name = "idclas")
    private Long idClasse;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idfepr", nullable = false)
    private FeuillePresenceEntity feuillePresence;
    @Embedded
    private Signature signature;

}
