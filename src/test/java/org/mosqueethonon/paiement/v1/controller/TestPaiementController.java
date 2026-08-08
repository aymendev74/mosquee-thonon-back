package org.mosqueethonon.paiement.v1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mosqueethonon.common.controller.TestController;
import org.mosqueethonon.inscription.entity.InscriptionEnfantEntity;
import org.mosqueethonon.inscription.entity.ResponsableLegalEntity;
import org.mosqueethonon.inscription.enums.StatutInscriptionEnum;
import org.mosqueethonon.inscription.repository.InscriptionEnfantRepository;
import org.mosqueethonon.paiement.enums.ModePaiementEnum;
import org.mosqueethonon.paiement.enums.StatutPaiementEnum;
import org.mosqueethonon.paiement.enums.StatutReglementEnum;
import org.mosqueethonon.paiement.enums.TypeCiblePaiementEnum;
import org.mosqueethonon.paiement.exception.PaiementErreurEnum;
import org.mosqueethonon.paiement.repository.PaiementRepository;
import org.mosqueethonon.paiement.v1.dto.PaiementDto;
import org.mosqueethonon.paiement.v1.dto.PaiementErreurDto;
import org.mosqueethonon.paiement.v1.dto.SituationPaiementDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestPaiementController extends TestController {

    private static final BigDecimal MONTANT_INSCRIPTION = bd(200);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private InscriptionEnfantRepository inscriptionEnfantRepository;

    private Long idInscription;

    @BeforeEach
    public void initInscription() {
        this.paiementRepository.deleteAll();
        this.inscriptionEnfantRepository.deleteAll();

        ResponsableLegalEntity responsableLegal = new ResponsableLegalEntity();
        responsableLegal.setNom("Dupont");
        responsableLegal.setPrenom("Karim");
        responsableLegal.setEmail("karim.dupont@example.org");

        InscriptionEnfantEntity inscription = new InscriptionEnfantEntity();
        inscription.setResponsableLegal(responsableLegal);
        inscription.setStatut(StatutInscriptionEnum.PROVISOIRE);
        inscription.setDateInscription(LocalDateTime.now());
        inscription.setMontantTotal(MONTANT_INSCRIPTION);
        inscription.setEleves(new ArrayList<>());

        this.idInscription = this.inscriptionEnfantRepository.save(inscription).getId();
    }

    // ---------------------------------------------------------------------------------------
    // Sécurité
    // ---------------------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "aymen", roles = {"ENSEIGNANT"})
    public void testGetSituationSansRoleAdminReturn403() throws Exception {
        // GIVEN un utilisateur qui n'est pas administrateur
        // WHEN / THEN l'accès aux paiements lui est refusé — /v1/paiements n'est déclaré nulle part
        // dans SecurityConfig et relève donc de anyRequest().hasRole("ADMIN")
        this.mockMvc.perform(get("/v1/paiements")
                        .param("typeCible", TypeCiblePaiementEnum.INSCRIPTION.name())
                        .param("idCible", String.valueOf(this.idInscription))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "aymen", roles = {"ENSEIGNANT"})
    public void testCreerPaiementSansRoleAdminReturn403() throws Exception {
        // GIVEN un utilisateur qui n'est pas administrateur
        // WHEN / THEN la saisie lui est refusée
        this.mockMvc.perform(post("/v1/paiements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.jsonMapper.writeValueAsString(this.paiement(bd(50), ModePaiementEnum.ESPECE)))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------------------------------
    // Cycle de vie complet
    // ---------------------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "aymen", roles = {"ADMIN"})
    public void testCycleCompletCreationModificationAnnulation() throws Exception {
        // GIVEN une inscription de 200 € sans aucun paiement
        SituationPaiementDto situation = this.getSituation();
        assertEquals(StatutReglementEnum.NON_REGLE, situation.getStatutReglement());
        assertEquals(0, MONTANT_INSCRIPTION.compareTo(situation.getResteAPayer()));

        // WHEN on encaisse 120 € en espèces
        situation = this.creer(this.paiement(bd(120), ModePaiementEnum.ESPECE));

        // THEN l'inscription est partiellement réglée
        assertEquals(StatutReglementEnum.PARTIEL, situation.getStatutReglement());
        assertEquals(0, bd(80).compareTo(situation.getResteAPayer()));
        assertEquals(1, situation.getPaiements().size());
        Long idPaiement = situation.getPaiements().get(0).getId();

        // WHEN on encaisse le solde par chèque
        PaiementDto solde = this.paiement(bd(80), ModePaiementEnum.CHEQUE);
        solde.setReference("1234567");
        situation = this.creer(solde);

        // THEN l'inscription est soldée
        assertEquals(StatutReglementEnum.SOLDE, situation.getStatutReglement());
        assertEquals(0, BigDecimal.ZERO.compareTo(situation.getResteAPayer()));

        // WHEN on ramène le premier paiement de 120 € à 100 €
        situation = this.modifier(idPaiement, this.paiement(bd(100), ModePaiementEnum.ESPECE));

        // THEN il reste 20 € à payer
        assertEquals(StatutReglementEnum.PARTIEL, situation.getStatutReglement());
        assertEquals(0, bd(20).compareTo(situation.getResteAPayer()));

        // WHEN on annule ce premier paiement
        situation = this.annuler(idPaiement);

        // THEN il reste visible dans l'historique, au statut ANNULE, et le reste à payer remonte
        assertEquals(2, situation.getPaiements().size());
        assertEquals(StatutPaiementEnum.ANNULE, situation.getPaiements().stream()
                .filter(paiement -> paiement.getId().equals(idPaiement))
                .findFirst().orElseThrow().getStatut());
        assertEquals(0, bd(120).compareTo(situation.getResteAPayer()));
        assertTrue(this.paiementRepository.findById(idPaiement).isPresent(),
                "Un paiement annulé n'est jamais supprimé de la base");

        // WHEN on tente de le modifier à nouveau
        // THEN il est figé
        assertEquals(PaiementErreurEnum.PAIEMENT_ANNULE_NON_MODIFIABLE.name(),
                this.creerEnErreur(put("/v1/paiements/" + idPaiement), this.paiement(bd(10), ModePaiementEnum.ESPECE)));
    }

    @Test
    @WithMockUser(username = "aymen", roles = {"ADMIN"})
    public void testModifierPaiementEnAugmentationNeCompteLExistantQuUneFois() throws Exception {
        // GIVEN une inscription de 200 € réglée par un unique paiement de 120 €
        SituationPaiementDto situation = this.creer(this.paiement(bd(120), ModePaiementEnum.ESPECE));
        Long idPaiement = situation.getPaiements().get(0).getId();

        // WHEN on porte ce paiement à 200 €
        situation = this.modifier(idPaiement, this.paiement(bd(200), ModePaiementEnum.CARTE));

        // THEN c'est accepté : le paiement modifié est retiré du déjà-réglé avant contrôle
        assertEquals(StatutReglementEnum.SOLDE, situation.getStatutReglement());
        assertEquals(1, situation.getPaiements().size());
    }

    // ---------------------------------------------------------------------------------------
    // Contrôles de saisie
    // ---------------------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "aymen", roles = {"ADMIN"})
    public void testCreerPaiementSuperieurAuResteReturn400AvecCode() throws Exception {
        // GIVEN une inscription de 200 € déjà réglée à 120 €
        this.creer(this.paiement(bd(120), ModePaiementEnum.ESPECE));

        // WHEN / THEN un règlement de 150 € est refusé, avec un code exploitable par le front
        assertEquals(PaiementErreurEnum.MONTANT_SUPERIEUR_RESTE.name(),
                this.creerEnErreur(post("/v1/paiements"), this.paiement(bd(150), ModePaiementEnum.ESPECE)));
        assertEquals(1, this.paiementRepository.findAll().size());
    }

    @Test
    @WithMockUser(username = "aymen", roles = {"ADMIN"})
    public void testCreerPaiementModeWebReturn400() throws Exception {
        // GIVEN une inscription de 200 €
        // WHEN / THEN le mode réservé au règlement en ligne est refusé en saisie manuelle
        assertEquals(PaiementErreurEnum.MODE_WEB_NON_AUTORISE.name(),
                this.creerEnErreur(post("/v1/paiements"), this.paiement(bd(50), ModePaiementEnum.WEB)));
    }

    @Test
    @WithMockUser(username = "aymen", roles = {"ADMIN"})
    public void testCreerPaiementDateFutureReturn400() throws Exception {
        // GIVEN une inscription de 200 €
        PaiementDto paiement = this.paiement(bd(50), ModePaiementEnum.ESPECE);
        paiement.setDatePaiement(LocalDate.now().plusDays(1));

        // WHEN / THEN un paiement daté de demain est refusé
        assertEquals(PaiementErreurEnum.DATE_FUTURE.name(),
                this.creerEnErreur(post("/v1/paiements"), paiement));
    }

    @Test
    @WithMockUser(username = "aymen", roles = {"ADMIN"})
    public void testCreerPaiementSurInscriptionInexistanteReturn400() throws Exception {
        // GIVEN une inscription qui n'existe pas
        PaiementDto paiement = this.paiement(bd(50), ModePaiementEnum.ESPECE);
        paiement.setIdCible(999_999L);

        // WHEN / THEN la cible est signalée comme introuvable
        assertEquals(PaiementErreurEnum.CIBLE_INTROUVABLE.name(),
                this.creerEnErreur(post("/v1/paiements"), paiement));
    }

    @Test
    @WithMockUser(username = "aymen", roles = {"ADMIN"})
    public void testAnnulerPaiementInexistantReturn404() throws Exception {
        // GIVEN un identifiant de paiement inconnu
        // WHEN / THEN la ressource est signalée absente
        this.mockMvc.perform(post("/v1/paiements/999999/annulation").with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------------------------------
    // Embarquement dans l'inscription
    // ---------------------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "aymen", roles = {"ADMIN"})
    public void testSituationEmbarqueeDansLInscription() throws Exception {
        // GIVEN une inscription de 200 € réglée à hauteur de 120 €
        this.creer(this.paiement(bd(120), ModePaiementEnum.ESPECE));

        // WHEN on charge l'inscription
        MvcResult result = this.mockMvc.perform(get("/v1/inscriptions-enfants/" + this.idInscription).with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        // THEN sa situation de règlement est déjà dedans, sans second appel
        SituationPaiementDto situation = this.jsonMapper
                .readTree(result.getResponse().getContentAsString())
                .get("situationPaiement")
                .traverse(this.jsonMapper)
                .readValueAs(SituationPaiementDto.class);
        assertNotNull(situation);
        assertEquals(StatutReglementEnum.PARTIEL, situation.getStatutReglement());
        assertEquals(0, bd(80).compareTo(situation.getResteAPayer()));
    }

    // ---------------------------------------------------------------------------------------
    // Utilitaires
    // ---------------------------------------------------------------------------------------

    private PaiementDto paiement(BigDecimal montant, ModePaiementEnum mode) {
        return PaiementDto.builder()
                .typeCible(TypeCiblePaiementEnum.INSCRIPTION)
                .idCible(this.idInscription)
                .montant(montant)
                .datePaiement(LocalDate.now())
                .mode(mode)
                .build();
    }

    private SituationPaiementDto getSituation() throws Exception {
        return this.lireSituation(this.mockMvc.perform(get("/v1/paiements")
                        .param("typeCible", TypeCiblePaiementEnum.INSCRIPTION.name())
                        .param("idCible", String.valueOf(this.idInscription))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn());
    }

    private SituationPaiementDto creer(PaiementDto paiement) throws Exception {
        return this.lireSituation(this.mockMvc.perform(post("/v1/paiements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.jsonMapper.writeValueAsString(paiement))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn());
    }

    private SituationPaiementDto modifier(Long id, PaiementDto paiement) throws Exception {
        return this.lireSituation(this.mockMvc.perform(put("/v1/paiements/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.jsonMapper.writeValueAsString(paiement))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn());
    }

    private SituationPaiementDto annuler(Long id) throws Exception {
        return this.lireSituation(this.mockMvc.perform(post("/v1/paiements/" + id + "/annulation").with(csrf()))
                .andExpect(status().isOk())
                .andReturn());
    }

    /**
     * @return le code d'erreur renvoyé dans le corps du 400
     */
    private String creerEnErreur(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requete,
                                 PaiementDto paiement) throws Exception {
        MvcResult result = this.mockMvc.perform(requete
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.jsonMapper.writeValueAsString(paiement))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andReturn();
        return this.jsonMapper.readValue(result.getResponse().getContentAsString(), PaiementErreurDto.class).getCode();
    }

    private SituationPaiementDto lireSituation(MvcResult result) throws Exception {
        return this.jsonMapper.readValue(result.getResponse().getContentAsString(), SituationPaiementDto.class);
    }

}
