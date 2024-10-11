package org.mosqueethonon.entity.utilisateur;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_history", schema = "moth")
@Data
public class LoginHistoryEntity {

    @Id
    @Column(name = "idlohi")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "dtlohiconnexion")
    private LocalDateTime dateConnexion;
    @Column(name = "txlohiuser")
    private String username;

}
