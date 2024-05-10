package org.mosqueethonon.v1.criterias;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.NiveauScolaireEnum;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.time.LocalDate;
import java.util.List;

@Data
public class InscriptionCriteria {

    private String nom;
    private String prenom;
    private String telephone;
    private StatutInscription statut;
    private Long nbDerniersJours;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate dateInscription;
    private List<NiveauScolaireEnum> niveaux;
    private List<NiveauInterneEnum> niveauxInternes;
    private String noInscription;
    private Long idPeriode;

}
