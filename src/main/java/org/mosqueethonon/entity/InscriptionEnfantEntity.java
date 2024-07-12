package org.mosqueethonon.entity;

import lombok.Data;

import jakarta.persistence.*;

import java.util.List;

@Entity
@DiscriminatorValue("ENFANT")
@Data
public class InscriptionEnfantEntity extends InscriptionEntity {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idinsc", nullable = false)
    private List<EleveEntity> eleves;

}
