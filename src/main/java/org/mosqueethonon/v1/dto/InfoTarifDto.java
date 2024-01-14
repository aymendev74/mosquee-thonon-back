package org.mosqueethonon.v1.dto;

import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.annotations.CodeTarif;

import java.math.BigDecimal;

@Data
@Builder
public class InfoTarifDto {

    private Long idPeriode;
    @CodeTarif("BASE_1_ENFANT")
    private BigDecimal montantBase1Enfant;
    @CodeTarif("BASE_ADHERENT_1_ENFANT")
    private BigDecimal montantBase1EnfantAdherent;
    @CodeTarif("ENFANT_1_ENFANT")
    private BigDecimal montantEnfant1Enfant;
    @CodeTarif("ENFANT_ADHERENT_1_ENFANT")
    private BigDecimal montantEnfant1EnfantAdherent;
    @CodeTarif("BASE_2_ENFANT")
    private BigDecimal montantBase2Enfant;
    @CodeTarif("BASE_ADHERENT_2_ENFANT")
    private BigDecimal montantBase2EnfantAdherent;
    @CodeTarif("ENFANT_2_ENFANT")
    private BigDecimal montantEnfant2Enfant;
    @CodeTarif("ENFANT_ADHERENT_2_ENFANT")
    private BigDecimal montantEnfant2EnfantAdherent;
    @CodeTarif("BASE_3_ENFANT")
    private BigDecimal montantBase3Enfant;
    @CodeTarif("BASE_ADHERENT_3_ENFANT")
    private BigDecimal montantBase3EnfantAdherent;
    @CodeTarif("ENFANT_3_ENFANT")
    private BigDecimal montantEnfant3Enfant;
    @CodeTarif("ENFANT_ADHERENT_3_ENFANT")
    private BigDecimal montantEnfant3EnfantAdherent;
    @CodeTarif("BASE_4_ENFANT")
    private BigDecimal montantBase4Enfant;
    @CodeTarif("BASE_ADHERENT_4_ENFANT")
    private BigDecimal montantBase4EnfantAdherent;
    @CodeTarif("ENFANT_4_ENFANT")
    private BigDecimal montantEnfant4Enfant;
    @CodeTarif("ENFANT_ADHERENT_4_ENFANT")
    private BigDecimal montantEnfant4EnfantAdherent;

}
