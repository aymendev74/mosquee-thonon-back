package org.mosqueethonon.paiement.v1.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.common.config.APIDateFormats;
import org.mosqueethonon.paiement.enums.ModePaiementEnum;
import org.mosqueethonon.paiement.enums.StatutPaiementEnum;
import org.mosqueethonon.paiement.enums.TypeCiblePaiementEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaiementDto {

    private Long id;
    private TypeCiblePaiementEnum typeCible;
    private Long idCible;
    private BigDecimal montant;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_FORMAT)
    private LocalDate datePaiement;
    private ModePaiementEnum mode;
    /**
     * En lecture seule : le statut n'est jamais piloté par le client, la création force VALIDE et
     * seule l'annulation le fait changer.
     */
    private StatutPaiementEnum statut;
    private String reference;
    private String commentaire;

}
