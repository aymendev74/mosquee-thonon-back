package org.mosqueethonon.inscription.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;
import org.mosqueethonon.inscription.enums.StatutInscriptionEnum;

@Entity
@Immutable
@Table(name = "v_info_mail_inscription")
@Data
public class InfoMailInscriptionEntity {

    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "nom")
    private String nom;
    @Column(name = "prenom")
    private String prenom;
    @Column(name = "email")
    private String email;
    @Column(name = "statut")
    @Enumerated(EnumType.STRING)
    private StatutInscriptionEnum statut;
    @Column(name = "noinscription")
    private String noInscription;

}
