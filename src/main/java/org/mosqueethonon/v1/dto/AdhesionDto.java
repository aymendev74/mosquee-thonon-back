package org.mosqueethonon.v1.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
public class AdhesionDto extends MailObjectDto {

    private Long id;
    private String titre;
    private String dateNaissance;
    private Long idTarif;
    private String telephone;
    private String mobile;
    private String numeroEtRue;
    private Integer codePostal;
    private String ville;
    private BigDecimal montantAutre;
    private StatutInscription statut;
    private String dateInscription;
    private BigDecimal montant;
    private Integer noMembre;
    private SignatureDto signature;

}
