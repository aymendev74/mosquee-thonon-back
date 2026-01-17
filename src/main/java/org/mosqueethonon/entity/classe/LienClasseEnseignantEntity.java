package org.mosqueethonon.entity.classe;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.entity.audit.Auditable;
import org.mosqueethonon.entity.audit.EntityListener;
import org.mosqueethonon.entity.audit.Signature;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;

@Entity
@EntityListeners(EntityListener.class)
@Table(name = "lien_classe_enseignant", schema = "moth")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LienClasseEnseignantEntity implements Auditable {

    @Id
    @Column(name = "idlcen")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "idclas", insertable = false, updatable = false)
    private Long idClasse;
    @Column(name = "idutil")
    private Long idUtilisateur;
    @ManyToOne
    @JoinColumn(name = "idutil", insertable = false, updatable = false)
    private UtilisateurEntity enseignant;
    @Embedded
    private Signature signature;

}
