package org.mosqueethonon.paiement.v1.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.mosqueethonon.paiement.enums.TypeCiblePaiementEnum;
import org.mosqueethonon.paiement.service.PaiementService;
import org.mosqueethonon.paiement.v1.dto.PaiementDto;
import org.mosqueethonon.paiement.v1.dto.SituationPaiementDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Réservé aux administrateurs : aucune route n'étant déclarée pour {@code /v1/paiements} dans
 * {@code SecurityConfig}, elles relèvent de la règle terminale {@code anyRequest().hasRole("ADMIN")}.
 * {@code TestPaiementController} fige ce comportement.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/v1/paiements")
public class PaiementController {

    private final PaiementService paiementService;

    @Operation(summary = "Situation de règlement d'une inscription")
    @GetMapping
    public ResponseEntity<SituationPaiementDto> getSituation(@RequestParam("typeCible") TypeCiblePaiementEnum typeCible,
                                                             @RequestParam("idCible") Long idCible) {
        return ResponseEntity.ok(this.paiementService.getSituation(typeCible, idCible));
    }

    @Operation(summary = "Enregistrement d'un paiement")
    @PostMapping
    public ResponseEntity<SituationPaiementDto> creerPaiement(@RequestBody PaiementDto paiement) {
        return ResponseEntity.ok(this.paiementService.creer(paiement));
    }

    @Operation(summary = "Modification d'un paiement")
    @PutMapping(path = "/{id}")
    public ResponseEntity<SituationPaiementDto> modifierPaiement(@PathVariable("id") Long id,
                                                                 @RequestBody PaiementDto paiement) {
        return ResponseEntity.ok(this.paiementService.modifier(id, paiement));
    }

    /**
     * Endpoint explicite plutôt qu'un {@code DELETE} : la ligne est conservée, seul son statut
     * change. Un {@code DELETE} laisserait croire à une suppression.
     */
    @Operation(summary = "Annulation d'un paiement")
    @PostMapping(path = "/{id}/annulation")
    public ResponseEntity<SituationPaiementDto> annulerPaiement(@PathVariable("id") Long id) {
        return ResponseEntity.ok(this.paiementService.annuler(id));
    }

}
