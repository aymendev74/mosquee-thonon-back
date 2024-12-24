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
@Table(name = "lien_eleve_feuille_presence", schema = "moth")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EleveFeuillePresenceEntity implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idlefp")
    private Long id;
    @Column(name = "idelev")
    private Long idEleve;
    @Column(name = "idfepr", insertable = false, updatable = false)
    private Long idFeuillePresence;
    @Column(name = "lolefppresent")
    private Boolean present;
    @Embedded
    private Signature signature;

}
