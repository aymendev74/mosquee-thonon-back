package org.mosqueethonon.v1.dto.inscription;

import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.enums.MatiereEnum;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.SexeEnum;
import org.mosqueethonon.enums.StatutProfessionnelEnum;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class InscriptionAdulteParAnneeScolaireDto {

    private Integer anneeDebut;
    private Integer anneeFin;
    private StatutInscription statut;
    private BigDecimal montantTotal;
    private String noInscription;
    
    // Informations de l'adulte inscrit
    private String nom;
    private String prenom;
    private String email;
    private LocalDate dateNaissance;
    private String mobile;
    private SexeEnum sexe;
    private NiveauInterneEnum niveauInterne;
    private StatutProfessionnelEnum statutProfessionnel;
    private List<MatiereEnum> matieres;
}
