package org.mosqueethonon.v1.dto.referentiel;

import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.annotations.CodeTarif;

import java.math.BigDecimal;

@Data
@Builder
public class InfoTarifDto {

    private Long idPeriode;
    // Tarifs spécifiques aux inscriptions enfants
    @CodeTarif(codeTarif = "BASE_1_ENFANT", type="BASE", adherent = false, nbEnfant = 1)
    private BigDecimal montantBase1Enfant;
    @CodeTarif(codeTarif = "BASE_ADHERENT_1_ENFANT", type="BASE", adherent = true, nbEnfant = 1)
    private BigDecimal montantBase1EnfantAdherent;
    @CodeTarif(codeTarif = "ENFANT_1_ENFANT", type="ENFANT", adherent = false, nbEnfant = 1)
    private BigDecimal montantEnfant1Enfant;
    @CodeTarif(codeTarif = "ENFANT_ADHERENT_1_ENFANT", type="ENFANT", adherent = true, nbEnfant = 1)
    private BigDecimal montantEnfant1EnfantAdherent;
    @CodeTarif(codeTarif = "BASE_2_ENFANT", type="BASE", adherent = false, nbEnfant = 2)
    private BigDecimal montantBase2Enfant;
    @CodeTarif(codeTarif = "BASE_ADHERENT_2_ENFANT", type="BASE", adherent = true, nbEnfant = 2)
    private BigDecimal montantBase2EnfantAdherent;
    @CodeTarif(codeTarif = "ENFANT_2_ENFANT", type="ENFANT", adherent = false, nbEnfant = 2)
    private BigDecimal montantEnfant2Enfant;
    @CodeTarif(codeTarif = "ENFANT_ADHERENT_2_ENFANT", type="ENFANT", adherent = true, nbEnfant = 2)
    private BigDecimal montantEnfant2EnfantAdherent;
    @CodeTarif(codeTarif = "BASE_3_ENFANT", type="BASE", adherent = false, nbEnfant = 3)
    private BigDecimal montantBase3Enfant;
    @CodeTarif(codeTarif = "BASE_ADHERENT_3_ENFANT", type="BASE", adherent = true, nbEnfant = 3)
    private BigDecimal montantBase3EnfantAdherent;
    @CodeTarif(codeTarif = "ENFANT_3_ENFANT", type="ENFANT", adherent = false, nbEnfant = 3)
    private BigDecimal montantEnfant3Enfant;
    @CodeTarif(codeTarif = "ENFANT_ADHERENT_3_ENFANT", type="ENFANT", adherent = true, nbEnfant = 3)
    private BigDecimal montantEnfant3EnfantAdherent;
    @CodeTarif(codeTarif = "BASE_4_ENFANT", type="BASE", adherent = false, nbEnfant = 4)
    private BigDecimal montantBase4Enfant;
    @CodeTarif(codeTarif = "BASE_ADHERENT_4_ENFANT", type="BASE", adherent = true, nbEnfant = 4)
    private BigDecimal montantBase4EnfantAdherent;
    @CodeTarif(codeTarif = "ENFANT_4_ENFANT", type="ENFANT", adherent = false, nbEnfant = 4)
    private BigDecimal montantEnfant4Enfant;
    @CodeTarif(codeTarif = "ENFANT_ADHERENT_4_ENFANT", type="ENFANT", adherent = true, nbEnfant = 4)
    private BigDecimal montantEnfant4EnfantAdherent;

    // Tarif spécifique aux inscriptions adulte
    private BigDecimal montant;
}
