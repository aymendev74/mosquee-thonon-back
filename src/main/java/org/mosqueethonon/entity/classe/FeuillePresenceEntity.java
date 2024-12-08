package org.mosqueethonon.entity.classe;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;

import java.time.LocalDate;
import java.util.List;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "feuille_presence", schema = "moth")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeuillePresenceEntity implements Auditable {

    @Id
    @Column(name = "idfepr")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "dtfeprdate")
    private LocalDate date;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idfepr", referencedColumnName = "idfepr", nullable = false)
    private List<EleveFeuillePresenceEntity> elevesFeuillesPresences;
    @Embedded
    private Signature signature;

}
