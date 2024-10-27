package org.mosqueethonon.v1.dto.referentiel;

import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.annotations.CodeTarifEnfant;
import org.mosqueethonon.annotations.TarifAdulte;
import org.mosqueethonon.enums.TypeTarifEnum;

import java.math.BigDecimal;

@Data
@Builder
public class InfoTarifDto {

    private Long idPeriode;
    // Tarifs spécifiques aux inscriptions enfants
    @CodeTarifEnfant(codeTarif = "BASE_1_ENFANT", type = TypeTarifEnum.BASE, adherent = false, nbEnfant = 1)
    private BigDecimal montantBase1Enfant;
    @CodeTarifEnfant(codeTarif = "BASE_ADHERENT_1_ENFANT", type = TypeTarifEnum.BASE, adherent = true, nbEnfant = 1)
    private BigDecimal montantBase1EnfantAdherent;
    @CodeTarifEnfant(codeTarif = "ENFANT_1_ENFANT", type = TypeTarifEnum.ENFANT, adherent = false, nbEnfant = 1)
    private BigDecimal montantEnfant1Enfant;
    @CodeTarifEnfant(codeTarif = "ENFANT_ADHERENT_1_ENFANT", type = TypeTarifEnum.ENFANT, adherent = true, nbEnfant = 1)
    private BigDecimal montantEnfant1EnfantAdherent;
    @CodeTarifEnfant(codeTarif = "BASE_2_ENFANT", type = TypeTarifEnum.BASE, adherent = false, nbEnfant = 2)
    private BigDecimal montantBase2Enfant;
    @CodeTarifEnfant(codeTarif = "BASE_ADHERENT_2_ENFANT", type = TypeTarifEnum.BASE, adherent = true, nbEnfant = 2)
    private BigDecimal montantBase2EnfantAdherent;
    @CodeTarifEnfant(codeTarif = "ENFANT_2_ENFANT", type = TypeTarifEnum.ENFANT, adherent = false, nbEnfant = 2)
    private BigDecimal montantEnfant2Enfant;
    @CodeTarifEnfant(codeTarif = "ENFANT_ADHERENT_2_ENFANT", type = TypeTarifEnum.ENFANT, adherent = true, nbEnfant = 2)
    private BigDecimal montantEnfant2EnfantAdherent;
    @CodeTarifEnfant(codeTarif = "BASE_3_ENFANT", type = TypeTarifEnum.BASE, adherent = false, nbEnfant = 3)
    private BigDecimal montantBase3Enfant;
    @CodeTarifEnfant(codeTarif = "BASE_ADHERENT_3_ENFANT", type = TypeTarifEnum.BASE, adherent = true, nbEnfant = 3)
    private BigDecimal montantBase3EnfantAdherent;
    @CodeTarifEnfant(codeTarif = "ENFANT_3_ENFANT", type = TypeTarifEnum.ENFANT, adherent = false, nbEnfant = 3)
    private BigDecimal montantEnfant3Enfant;
    @CodeTarifEnfant(codeTarif = "ENFANT_ADHERENT_3_ENFANT", type = TypeTarifEnum.ENFANT, adherent = true, nbEnfant = 3)
    private BigDecimal montantEnfant3EnfantAdherent;
    @CodeTarifEnfant(codeTarif = "BASE_4_ENFANT", type = TypeTarifEnum.BASE, adherent = false, nbEnfant = 4)
    private BigDecimal montantBase4Enfant;
    @CodeTarifEnfant(codeTarif = "BASE_ADHERENT_4_ENFANT", type = TypeTarifEnum.BASE, adherent = true, nbEnfant = 4)
    private BigDecimal montantBase4EnfantAdherent;
    @CodeTarifEnfant(codeTarif = "ENFANT_4_ENFANT", type = TypeTarifEnum.BASE, adherent = false, nbEnfant = 4)
    private BigDecimal montantEnfant4Enfant;
    @CodeTarifEnfant(codeTarif = "ENFANT_ADHERENT_4_ENFANT", type = TypeTarifEnum.ENFANT, adherent = true, nbEnfant = 4)
    private BigDecimal montantEnfant4EnfantAdherent;

    // Tarif spécifique aux inscriptions adulte
    @TarifAdulte(type = TypeTarifEnum.ETUDIANT)
    private BigDecimal montantEtudiant;
    @TarifAdulte(type = TypeTarifEnum.AVEC_ACTIVITE)
    private BigDecimal montantAvecActivite;
    @TarifAdulte(type = TypeTarifEnum.SANS_ACTIVITE)
    private BigDecimal montantSansActivite;
}
