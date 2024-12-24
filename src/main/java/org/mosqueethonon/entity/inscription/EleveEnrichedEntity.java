package org.mosqueethonon.entity.inscription;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.NiveauScolaireEnum;
import org.mosqueethonon.enums.ResultatEnum;

import java.time.LocalDate;

@Entity
@Table(name = "v_eleve_enriched", schema = "moth")
@Data
@Immutable
public class EleveEnrichedEntity {

    @Column(name = "id")
    @Id
    private Long id;
    @Column(name = "idclasse")
    private Long idClasse;
    @Column(name = "nom")
    private String nom;
    @Column(name = "prenom")
    private String prenom;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    @Column(name = "datenaissance")
    private LocalDate dateNaissance;
    @Column(name = "niveauinterne")
    @Enumerated(EnumType.STRING)
    private NiveauInterneEnum niveauInterne;
    @Column(name = "mobile")
    private String mobile;
    @Column(name = "mobilecontacturgence")
    private String mobileContactUrgence;
    @Column(name = "autorisationautonomie")
    private Boolean autorisationAutonomie;
    @Column(name = "autorisationmedia")
    private Boolean autorisationMedia;
    @Column(name = "nomresponsablelegal")
    private String nomResponsableLegal;
    @Column(name = "prenomresponsablelegal")
    private String prenomResponsableLegal;
    @Column(name = "nomcontacturgence")
    private String nomContactUrgence;
    @Column(name = "prenomcontacturgence")
    private String prenomContactUrgence;
    @Column(name = "resultat")
    @Enumerated(EnumType.STRING)
    private ResultatEnum resultat;

}
