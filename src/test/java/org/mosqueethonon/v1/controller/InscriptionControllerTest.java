package org.mosqueethonon.v1.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mosqueethonon.entity.InscriptionEntity;
import org.mosqueethonon.entity.TarifEntity;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.NiveauScolaireEnum;
import org.mosqueethonon.v1.dto.EleveDto;
import org.mosqueethonon.v1.dto.InscriptionDto;
import org.mosqueethonon.v1.dto.ResponsableLegalDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InscriptionControllerTest extends ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jsonMapper;

    @Test
    public void testFindInscriptionsByCriteriaReturn200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/inscriptions")
                        .header("Authorization", generateToken()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testSaveInscriptionMultiThreading() throws Exception {
        int nbThreads = 510;
        CountDownLatch compteur = new CountDownLatch(nbThreads);

        for (int i = 0; i < nbThreads; i++) {
            new Thread(() -> {
                try {
                    mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/inscriptions")
                                    .header("Authorization", generateToken())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(this.createInscription())))
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
        List<InscriptionEntity> allInscriptions = this.inscriptionRepository.findAll();
        assertEquals(510, allInscriptions.size());
        Long nbInscriptionEnAttente = allInscriptions.stream().filter(inscription ->
                inscription.getStatut() == StatutInscription.LISTE_ATTENTE).count();
        Long nbInscriptionProvisoire = allInscriptions.stream().filter(inscription ->
                inscription.getStatut() == StatutInscription.PROVISOIRE).count();
        assertEquals(500, nbInscriptionProvisoire);
        assertEquals(10, nbInscriptionEnAttente);
    }

    private InscriptionDto createInscription() {
        return InscriptionDto.builder().eleves(this.createEleve())
                .responsableLegal(createResponsableLegal()).build();
    }

    private ResponsableLegalDto createResponsableLegal() {
        return ResponsableLegalDto.builder().adherent(true).autorisationAutonomie(true).autorisationMedia(false)
                .codePostal(74200).mobile("").ville("").nomAutre("").lienParente("").prenomAutre("")
                .numeroEtRue("").telephone("").nom("").prenom("").build();
    }

    private List<EleveDto> createEleve() {
        return Lists.newArrayList(EleveDto.builder().nom("").prenom("").dateNaissance("14.11.2015")
                .niveau(NiveauScolaireEnum.CE2).niveauInterne(NiveauInterneEnum.PREPARATOIRE).build());
    }

}