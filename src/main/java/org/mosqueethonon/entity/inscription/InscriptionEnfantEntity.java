package org.mosqueethonon.entity.inscription;

import lombok.Data;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("ENFANT")
@Data
public class InscriptionEnfantEntity extends InscriptionEntity {

}
