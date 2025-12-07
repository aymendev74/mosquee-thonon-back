package org.mosqueethonon.v1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mosqueethonon.entity.inscription.InscriptionAdulteEntity;
import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.mosqueethonon.enums.ApplicationTarifEnum;
import org.mosqueethonon.enums.StatutProfessionnelEnum;
import org.mosqueethonon.enums.TypeTarifEnum;
import org.mosqueethonon.repository.InscriptionAdulteRepository;
import org.mosqueethonon.v1.dto.inscription.InscriptionAdulteDto;
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
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TestInscriptionAdulteController extends TestController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    protected InscriptionAdulteRepository inscriptionAdulteRepository;


    @BeforeAll
    protected void initReferentiel() {
        this.initTarifsCoursAdulte(this.initPeriodeCoursAdulte());
    }

    private void initTarifsCoursAdulte(PeriodeEntity periode) {
        List<TarifEntity> tarifsCours = new ArrayList<>();

        // tarifs adultes
        tarifsCours.add(TarifEntity.builder().periode(periode).type(TypeTarifEnum.ETUDIANT).montant(bd(15)).build());
        tarifsCours.add(TarifEntity.builder().periode(periode).type(TypeTarifEnum.SANS_ACTIVITE).montant(bd(20)).build());
        tarifsCours.add(TarifEntity.builder().periode(periode).type(TypeTarifEnum.AVEC_ACTIVITE).montant(bd(40)).build());

        this.tarifRepository.saveAll(tarifsCours);
    }

    private PeriodeEntity initPeriodeCoursAdulte() {
        LocalDate today = LocalDate.now();
        PeriodeEntity periode = PeriodeEntity.builder().application(ApplicationTarifEnum.COURS_ADULTE.name()).dateDebut(today.minusDays(1))
                .dateFin(today.plusDays(1)).build();
        return this.periodeRepository.save(periode);
    }

    @BeforeEach
    protected void deleteInscription() {
        this.inscriptionAdulteRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "anonymous")
    public void testSaveInscriptionAdulteMultiThreading() throws Exception {
        int nbThreads = 50;
        CountDownLatch compteur = new CountDownLatch(nbThreads);

        for (int i = 0; i < nbThreads; i++) {
            new Thread(() -> {
                try {
                    mockMvc.perform(MockMvcRequestBuilders.post("/v1/inscriptions-adultes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(this.createInscriptionAdulte()))
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

        // Puis on va vérifier que 510 inscriptions ont bien été enregistrées
        List<InscriptionAdulteEntity> allInscriptions = this.inscriptionAdulteRepository.findAll();
        // Vérification d'un certain nombre de critères (ajustez selon vos besoins)
        Long nbInscriptionsValides = allInscriptions.stream()
                .filter(inscription -> inscription.getStatut() == StatutInscription.PROVISOIRE)
                .count();
        assertEquals(50, nbInscriptionsValides);
    }

    private InscriptionAdulteDto createInscriptionAdulte() {
        InscriptionAdulteDto inscriptionDto = new InscriptionAdulteDto();
        inscriptionDto.setNom("Doe");
        inscriptionDto.setPrenom("John");
        inscriptionDto.setEmail("john.doe@example.com");
        inscriptionDto.setMobile("0600000000");
        inscriptionDto.setNumeroEtRue("10bis, rue de la mosquee");
        inscriptionDto.setCodePostal(74200);
        inscriptionDto.setVille("Thonon");
        inscriptionDto.setDateNaissance(LocalDate.of(1990, 1, 1));
        inscriptionDto.setStatutProfessionnel(StatutProfessionnelEnum.AVEC_ACTIVITE);
        return inscriptionDto;
    }

}
