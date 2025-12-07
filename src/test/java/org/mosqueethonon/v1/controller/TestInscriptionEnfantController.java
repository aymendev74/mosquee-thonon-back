package org.mosqueethonon.v1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mosqueethonon.entity.ParamEntity;
import org.mosqueethonon.entity.inscription.InscriptionEnfantEntity;
import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.mosqueethonon.enums.*;
import org.mosqueethonon.repository.InscriptionEnfantRepository;
import org.mosqueethonon.repository.ParamRepository;
import org.mosqueethonon.v1.dto.inscription.EleveDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantDto;
import org.mosqueethonon.v1.dto.inscription.ResponsableLegalDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TestInscriptionEnfantController extends TestController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    private ParamRepository paramRepository;

    @Autowired
    protected InscriptionEnfantRepository inscriptionEnfantRepository;

    @BeforeAll
    protected void initReferentiel() {
        this.initParams();
        this.initTarifsCoursEnfant(this.initPeriodeCoursEnfant());
    }

    private void initParams() {
        ParamEntity paramInscriptionEnabled = new ParamEntity();
        paramInscriptionEnabled.setName(ParamNameEnum.INSCRIPTION_ENFANT_ENABLED_FROM_DATE);
        paramInscriptionEnabled.setValue("01.01.1950");
        this.paramRepository.save(paramInscriptionEnabled);
    }

    private void initTarifsCoursEnfant(PeriodeEntity periode) {
        List<TarifEntity> tarifsCours = new ArrayList<>();
        // tarifs base
        tarifsCours.add(TarifEntity.builder().nbEnfant(1).periode(periode).adherent(true)
                .type(TypeTarifEnum.BASE).code("BASE_ADHERENT_1_ENFANT").montant(bd(120)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(1).periode(periode).adherent(false)
                .type(TypeTarifEnum.BASE).code("BASE_1_ENFANT").montant(bd(240)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(2).periode(periode).adherent(true)
                .type(TypeTarifEnum.BASE).code("BASE_ADHERENT_2_ENFANT").montant(bd(160)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(2).periode(periode).adherent(false)
                .type(TypeTarifEnum.BASE).code("BASE_2_ENFANT").montant(bd(280)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(3).periode(periode).adherent(true)
                .type(TypeTarifEnum.BASE).code("BASE_ADHERENT_3_ENFANT").montant(bd(200)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(3).periode(periode).adherent(false)
                .type(TypeTarifEnum.BASE).code("BASE_3_ENFANT").montant(bd(320)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(4).periode(periode).adherent(true)
                .type(TypeTarifEnum.BASE).code("BASE_ADHERENT_4_ENFANT").montant(bd(240)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(4).periode(periode).adherent(false)
                .type(TypeTarifEnum.BASE).code("BASE_4_ENFANT").montant(bd(360)).build());

        // tarifs par enfant
        tarifsCours.add(TarifEntity.builder().nbEnfant(1).periode(periode).adherent(true)
                .type(TypeTarifEnum.ENFANT).code("ENFANT_ADHERENT_1_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(1).periode(periode).adherent(false)
                .type(TypeTarifEnum.ENFANT).code("ENFANT_1_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(2).periode(periode).adherent(true)
                .type(TypeTarifEnum.ENFANT).code("ENFANT_ADHERENT_2_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(2).periode(periode).adherent(false)
                .type(TypeTarifEnum.ENFANT).code("ENFANT_2_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(3).periode(periode).adherent(true)
                .type(TypeTarifEnum.ENFANT).code("ENFANT_ADHERENT_3_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(3).periode(periode).adherent(false)
                .type(TypeTarifEnum.ENFANT).code("ENFANT_3_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(4).periode(periode).adherent(true)
                .type(TypeTarifEnum.ENFANT).code("ENFANT_ADHERENT_4_ENFANT").montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().nbEnfant(4).periode(periode).adherent(false)
                .type(TypeTarifEnum.ENFANT).code("ENFANT_4_ENFANT").montant(bd(15)).build());

        this.tarifRepository.saveAll(tarifsCours);
    }

    @BeforeEach
    protected void deleteInscription() {
        this.inscriptionEnfantRepository.deleteAll();
    }

    private PeriodeEntity initPeriodeCoursEnfant() {
        LocalDate today = LocalDate.now();
        PeriodeEntity periode = PeriodeEntity.builder().application(ApplicationTarifEnum.COURS_ENFANT.name()).dateDebut(today.minusDays(1))
                .dateFin(today.plusDays(1)).nbMaxInscription(500).build();
        return this.periodeRepository.save(periode);
    }

    @Test
    @WithMockUser(username = "anonymous")
    public void testSaveInscriptionMultiThreading() throws Exception {
        int nbThreads = 510;
        CountDownLatch compteur = new CountDownLatch(nbThreads);

        for (int i = 0; i < nbThreads; i++) {
            new Thread(() -> {
                try {
                    mockMvc.perform(MockMvcRequestBuilders.post("/v1/inscriptions-enfants")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(this.createInscription()))
                            .with(SecurityMockMvcRequestPostProcessors.csrf()))
                            .andExpect(MockMvcResultMatchers.status().isOk());
                } catch (Exception e) {
                    fail(e);
                } finally {
                    compteur.countDown();
                }

            }).start();
        }

        // On attend que tous les threads aient terminé leur inscription
        compteur.await();

        // Puis on va vérifier que 510 inscriptions ont bien été enregistrés dont 10 avec statut en attente
        List<InscriptionEnfantEntity> allInscriptions = this.inscriptionEnfantRepository.findAll();
        assertInscriptionsAttentes(allInscriptions,  10);

        // Puis on va supprimer 5 inscriptions provisoires pour forcer la mise à jour de la liste d'attente
        Set<Long> inscriptionsASupprimer = new HashSet<>();
        for(InscriptionEnfantEntity inscription : allInscriptions) {
            if(inscriptionsASupprimer.size() == 5) break;
            if(inscription.getStatut() == StatutInscription.PROVISOIRE) {
                inscriptionsASupprimer.add(inscription.getId());
            }
        }
        this.inscriptionOrchestratorService.deleteInscriptions(inscriptionsASupprimer);
        // Puis on va vérifier que 500 inscriptions sont bien toujours provisoires 5 sont en attentes
        allInscriptions = this.inscriptionEnfantRepository.findAll();
        assertInscriptionsAttentes(allInscriptions, 5);
    }

    private void assertInscriptionsAttentes(List<InscriptionEnfantEntity> allInscriptions, Integer nbEnAttente) {
        assertEquals(500 + nbEnAttente, allInscriptions.size());
        Long nbInscriptionEnAttente = allInscriptions.stream().filter(inscription ->
                inscription.getStatut() == StatutInscription.LISTE_ATTENTE).count();
        Long nbInscriptionProvisoire = allInscriptions.stream().filter(inscription ->
                inscription.getStatut() == StatutInscription.PROVISOIRE).count();
        assertEquals(500, nbInscriptionProvisoire);
        assertEquals(Long.valueOf(nbEnAttente), nbInscriptionEnAttente);
    }

    private InscriptionEnfantDto createInscription() {
        return InscriptionEnfantDto.builder().eleves(this.createEleve())
                .responsableLegal(createResponsableLegal()).build();
    }

    private ResponsableLegalDto createResponsableLegal() {
        return ResponsableLegalDto.builder().adherent(true).autorisationAutonomie(true).autorisationMedia(false)
                .codePostal(74200).mobile("").ville("").nomAutre("").lienParente("").prenomAutre("")
                .numeroEtRue("").nom("").prenom("").build();
    }

    private List<EleveDto> createEleve() {
        return Lists.newArrayList(EleveDto.builder().nom("").prenom("").dateNaissance(LocalDate.of(2015, 11, 14))
                .niveau(NiveauScolaireEnum.CE2).niveauInterne(NiveauInterneEnum.P1).build());
    }

}
