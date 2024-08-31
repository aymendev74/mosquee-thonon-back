package org.mosqueethonon.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@DiscriminatorValue("ADULTE")
@Data
public class InscriptionAdulteEntity extends InscriptionEntity {

}
