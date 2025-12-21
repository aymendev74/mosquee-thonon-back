package org.mosqueethonon.v1.controller;

import org.junit.jupiter.api.Test;
import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class TestPeriodeController extends TestController {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "user", roles = {"ENSEIGNANT"})
    public void testDeletePeriodeWithRoleEnseignantReturn403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/periodes/1")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = {"TRESORIER"})
    public void testDeletePeriodeWithRoleTresorierReturn403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/periodes/1")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeletePeriodeWithRoleAdminReturn204() throws Exception {
        PeriodeEntity periode = PeriodeEntity.builder()
                .dateDebut(LocalDate.of(2024, 9, 1))
                .dateFin(LocalDate.of(2025, 6, 30))
                .anneeDebut(2024)
                .anneeFin(2025)
                .application("COURS_ENFANT")
                .nbMaxInscription(100)
                .build();
        periode = periodeRepository.save(periode);

        TarifEntity tarif = TarifEntity.builder()
                .periode(periode)
                .montant(BigDecimal.valueOf(150))
                .code("TARIF_TEST")
                .build();
        tarifRepository.save(tarif);

        Long periodeId = periode.getId();

        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/periodes/" + periodeId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        assertFalse(periodeRepository.findById(periodeId).isPresent());
        assertEquals(0, tarifRepository.findByPeriodeId(periodeId).size());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDeletePeriodeNotFoundReturn404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/periodes/999999")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testCreatePeriodeWithRoleAdminReturn200() throws Exception {
        String periodeJson = """
                {
                    "dateDebut": "01.09.2025",
                    "dateFin": "30.06.2026",
                    "anneeDebut": 2025,
                    "anneeFin": 2026,
                    "application": "COURS_ENFANT",
                    "nbMaxInscription": 120
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/periodes")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(periodeJson))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testUpdatePeriodeWithRoleAdminReturn200() throws Exception {
        PeriodeEntity periode = PeriodeEntity.builder()
                .dateDebut(LocalDate.of(2024, 9, 1))
                .dateFin(LocalDate.of(2025, 6, 30))
                .anneeDebut(2024)
                .anneeFin(2025)
                .application("COURS_ENFANT")
                .nbMaxInscription(100)
                .build();
        periode = periodeRepository.save(periode);

        String periodeJson = """
                {
                    "dateDebut": "01.09.2024",
                    "dateFin": "30.06.2025",
                    "anneeDebut": 2024,
                    "anneeFin": 2025,
                    "application": "COURS_ENFANT",
                    "nbMaxInscription": 150
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/periodes/" + periode.getId())
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(periodeJson))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
