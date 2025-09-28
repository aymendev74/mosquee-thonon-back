package org.mosqueethonon.entity.referentiel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Immutable
@Getter
@Setter
@Entity
@Table(name = "ref_traduction", schema = "moth")
public class TraductionEntity {

    @Id
    @Column(name = "idtrad")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cdtradcle")
    private String cle;
    @Column(name = "cdtradvaleur")
    private String valeur;
    @Column(name = "txtradfr")
    private String fr;

}
