package org.mosqueethonon.v1.dto.adhesion;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.utils.StringUtils;
import org.mosqueethonon.v1.dto.IMailObject;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
public class AdhesionDto implements IMailObject {

    private String titre;
    private String nom;
    private String prenom;
    private String email;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate dateNaissance;
    private Long idTarif;
    private String mobile;
    private String numeroEtRue;
    private Integer codePostal;
    private String ville;
    private BigDecimal montantAutre;
    private StatutInscription statut;
    private BigDecimal montant;
    private Integer noMembre;

    public void normalize() {
        this.setNom(StringUtils.normalize(this.getNom()));
        this.setPrenom(StringUtils.normalize(this.getPrenom()));
    }
}
