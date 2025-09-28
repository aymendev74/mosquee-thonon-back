package org.mosqueethonon.entity.referentiel;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.mosqueethonon.enums.MatiereEnum;
import org.mosqueethonon.enums.TypeMatiereEnum;

@Entity
@Table(name = "ref_matiere", schema = "moth")
@Data
@Immutable
@NoArgsConstructor
@AllArgsConstructor
public class MatiereEntity {

    @Id
    @Column(name = "idmati")
    private Long id;
    @Column(name = "cdmaticode")
    @Enumerated(EnumType.STRING)
    private MatiereEnum code;
    @Column(name = "cdmatitype")
    @Enumerated(EnumType.STRING)
    private TypeMatiereEnum type;

}
