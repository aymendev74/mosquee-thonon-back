package org.mosqueethonon.entity.inscription;

import lombok.Data;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("ENFANT")
@Data
@EqualsAndHashCode(callSuper = true)
public class InscriptionEnfantEntity extends InscriptionEntity {

}
