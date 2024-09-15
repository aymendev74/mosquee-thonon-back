package org.mosqueethonon.entity.inscription;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@DiscriminatorValue("ADULTE")
@Data
public class InscriptionAdulteEntity extends InscriptionEntity {

}
