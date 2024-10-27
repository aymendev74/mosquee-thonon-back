package org.mosqueethonon.v1.dto.inscription;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.SexeEnum;
import org.mosqueethonon.enums.StatutProfessionnelEnum;
import org.mosqueethonon.utils.StringUtils;
import org.mosqueethonon.v1.dto.IMailObject;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class InscriptionAdulteDto implements IMailObject {

    private String nom;
    private String prenom;
    private String email;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate dateNaissance;
    private String mobile;
    private String numeroEtRue;
    private Integer codePostal;
    private String ville;
    private StatutInscription statut;
    private BigDecimal montantTotal;
    private NiveauInterneEnum niveauInterne;
    private SexeEnum sexe;
    private String anneeScolaire;
    private StatutProfessionnelEnum statutProfessionnel;

    public void normalize() {
        this.nom = StringUtils.normalize(StringUtils.normalize(nom));
        this.prenom = StringUtils.normalize(StringUtils.normalize(prenom));
    }
}
