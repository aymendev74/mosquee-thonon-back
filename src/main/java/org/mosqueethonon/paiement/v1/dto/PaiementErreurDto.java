package org.mosqueethonon.paiement.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Corps renvoyé quand une règle de saisie d'un paiement est violée.
 * <p>
 * Le {@code code} permet au front de choisir son message ; le {@code message} sert au diagnostic et
 * de repli si le code lui est inconnu.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaiementErreurDto {

    private String code;
    private String message;

}
