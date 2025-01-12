package org.mosqueethonon.entity.inscription;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mosqueethonon.enums.StatutProfessionnelEnum;

@Entity
@DiscriminatorValue("ADULTE")
@Data
@EqualsAndHashCode(callSuper = true)
public class InscriptionAdulteEntity extends InscriptionEntity {

    @Column(name = "cdinscstatutpro")
    private StatutProfessionnelEnum statutProfessionnel;

}
