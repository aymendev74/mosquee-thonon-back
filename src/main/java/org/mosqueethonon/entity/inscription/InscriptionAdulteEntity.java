package org.mosqueethonon.entity.inscription;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mosqueethonon.enums.StatutProfessionnelEnum;

import java.util.List;

@Entity
@DiscriminatorValue("ADULTE")
@Data
@EqualsAndHashCode(callSuper = true)
public class InscriptionAdulteEntity extends InscriptionEntity {

    @Column(name = "cdinscstatutpro")
    private StatutProfessionnelEnum statutProfessionnel;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idinsc")
    private List<InscriptionMatiereEntity> matieres;

}
