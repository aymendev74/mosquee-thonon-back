package org.mosqueethonon.v1.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mosqueethonon.entity.inscription.InscriptionEnfantEntity;
import org.mosqueethonon.enums.NiveauInterneEnum;
import org.mosqueethonon.enums.NiveauScolaireEnum;
import org.mosqueethonon.v1.dto.inscription.EleveDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantDto;
import org.mosqueethonon.v1.dto.inscription.ResponsableLegalDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class InscriptionEnfantControllerTest extends ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jsonMapper;

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
