package org.mosqueethonon.entity.classe;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;
import org.mosqueethonon.enums.JourActiviteEnum;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "classe_activite", schema = "moth")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClasseActiviteEntity implements Auditable {

    @Id
    @Column(name = "idclac")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cdclacjour")
    private JourActiviteEnum jour;
    @Embedded
    private Signature signature;

}
