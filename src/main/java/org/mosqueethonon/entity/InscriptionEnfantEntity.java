package org.mosqueethonon.entity;

import lombok.Data;

import jakarta.persistence.*;

import java.util.List;

@Entity
@DiscriminatorValue("ENFANT")
@Data
public class InscriptionEnfantEntity extends InscriptionEntity {

}
