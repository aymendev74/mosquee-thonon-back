package org.mosqueethonon.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.mosqueethonon.enums.ParamNameEnum;

@Entity
@Table(name = "params", schema = "moth")
@Data
public class ParamEntity {

    @Id
    @Column(name = "idpara")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "txparaname")
    @Enumerated(EnumType.STRING)
    private ParamNameEnum name;
    @Column(name = "txparavalue")
    private String value;

}
