package org.mosqueethonon.entity.inscription;

import jakarta.persistence.*;
import lombok.Data;
import org.mosqueethonon.enums.StatutProfessionnelEnum;

@Entity
@DiscriminatorValue("ADULTE")
@Data
public class InscriptionAdulteEntity extends InscriptionEntity {

    @Column(name = "cdinscstatutpro")
    private StatutProfessionnelEnum statutProfessionnel;

}
