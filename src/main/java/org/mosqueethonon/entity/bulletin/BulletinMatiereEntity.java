package org.mosqueethonon.entity.bulletin;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.Signature;
import org.mosqueethonon.enums.NoteMatiereEnum;

@Entity
@Table(name = "bulletin_matiere", schema = "moth")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulletinMatiereEntity implements Auditable {

    @Id
    @Column(name = "idbuma")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "idmati")
    private Long idMatiere;
    @Column(name = "cdbumanote")
    @Enumerated(EnumType.STRING)
    private NoteMatiereEnum note;
    @Column(name = "txbumaremarque")
    private String remarque;
    @Embedded
    private Signature signature;

}
