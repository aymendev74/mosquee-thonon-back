package org.mosqueethonon.v1.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mosqueethonon.entity.inscription.InscriptionAdulteEntity;
import org.mosqueethonon.v1.dto.inscription.InscriptionAdulteDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class InscriptionAdulteControllerTest extends ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jsonMapper;

    @Test
    public void testSaveInscriptionAdulteMultiThreading() throws Exception {
        int nbThreads = 50;
        CountDownLatch compteur = new CountDownLatch(nbThreads);

        for (int i = 0; i < nbThreads; i++) {
            new Thread(() -> {
                try {
                    mockMvc.perform(MockMvcRequestBuilders.post("/v1/inscriptions-adultes")
                                    .header("Authorization", generateToken(null))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(this.createInscriptionAdulte())))
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
        return inscriptionDto;
    }

}
