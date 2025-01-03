package org.mosqueethonon.entity.classe;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Formula;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "enseignant", schema = "moth")
@Data
public class EnseignantEntity implements Auditable {

    @Id
    @Column(name = "idense")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "txenseusername")
    private String username;
    @Column(name = "txensenom")
    private String nom;
    @Column(name = "txenseprenom")
    private String prenom;
    @Column(name = "txensemobile")
    private String mobile;
    @Formula("(select true from moth.classe clas where clas.idense = idense limit 1)")
    private Boolean hasClasse;
    @Embedded
    private Signature signature;

}
