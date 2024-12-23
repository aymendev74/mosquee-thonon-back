package org.mosqueethonon.entity.referentiel;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.enums.NiveauInterneEnum;

@Entity
@Table(name = "ref_niveau", schema = "moth")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NiveauEntity {

    @Column(name = "idreni")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @Column(name = "cdreniniveau")
    @Enumerated(EnumType.STRING)
    private NiveauInterneEnum niveau;
    @Column(name = "cdreniniveausup")
    @Enumerated(EnumType.STRING)
    private NiveauInterneEnum niveauSuperieur;

}
