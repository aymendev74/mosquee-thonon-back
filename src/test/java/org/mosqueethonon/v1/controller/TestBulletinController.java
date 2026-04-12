package org.mosqueethonon.v1.controller;

import org.junit.jupiter.api.Test;
import org.mosqueethonon.entity.bulletin.BulletinEntity;
import org.mosqueethonon.repository.BulletinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestBulletinController extends TestController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BulletinRepository bulletinRepository;

    // -----------------------------------------------------------------------
    // POST /v1/bulletins — création
    // -----------------------------------------------------------------------

    @Test
    public void testCreateBulletinSansAuthenticationRetourne401() throws Exception {
        String bulletinJson = """
                {
                    "idEleve": 1,
                    "mois": 3,
                    "annee": 2025,
                    "bulletinMatieres": []
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/bulletins")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bulletinJson))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "enseignant", roles = {"ENSEIGNANT"})
    public void testCreateBulletinAvecRoleEnseignantRetourne200() throws Exception {
        String bulletinJson = """
                {
                    "idEleve": 1,
                    "mois": 3,
                    "annee": 2025,
                    "nbAbsences": 0,
                    "bulletinMatieres": []
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/bulletins")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bulletinJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(username = "tresorier", roles = {"TRESORIER"})
    public void testCreateBulletinAvecRoleTresorierRetourne403() throws Exception {
        String bulletinJson = """
                {
                    "idEleve": 1,
                    "mois": 3,
                    "annee": 2025,
                    "bulletinMatieres": []
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/bulletins")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bulletinJson))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    // -----------------------------------------------------------------------
    // PUT /v1/bulletins/{id} — mise à jour
    // -----------------------------------------------------------------------

    @Test
    @WithMockUser(username = "enseignant", roles = {"ENSEIGNANT"})
    public void testUpdateBulletinAvecRoleEnseignantEtBulletinInexistantRetourne404() throws Exception {
        String bulletinJson = """
                {
                    "idEleve": 1,
                    "mois": 4,
                    "annee": 2025,
                    "bulletinMatieres": []
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/bulletins/999999999")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bulletinJson))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(username = "enseignant", roles = {"ENSEIGNANT"})
    public void testUpdateBulletinAvecRoleEnseignantRetourne200() throws Exception {
        // GIVEN — bulletin existant en base
        BulletinEntity bulletin = new BulletinEntity();
        bulletin.setIdEleve(1L);
        bulletin = bulletinRepository.save(bulletin);
        Long bulletinId = bulletin.getId();

        // L'id doit être inclus dans le body pour que le mapper ne le réinitialise pas
        String bulletinJson = String.format("""
                {
                    "id": %d,
                    "idEleve": 1,
                    "mois": 5,
                    "annee": 2025,
                    "nbAbsences": 1,
                    "bulletinMatieres": []
                }
                """, bulletinId);

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/bulletins/" + bulletinId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bulletinJson))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    // -----------------------------------------------------------------------
    // DELETE /v1/bulletins/{id} — suppression
    // -----------------------------------------------------------------------

    @Test
    public void testDeleteBulletinSansAuthenticationRetourne401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/bulletins/1")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "enseignant", roles = {"ENSEIGNANT"})
    public void testDeleteBulletinAvecRoleEnseignantRetourne200() throws Exception {
        // GIVEN — bulletin existant en base
        BulletinEntity bulletin = new BulletinEntity();
        bulletin.setIdEleve(1L);
        bulletin = bulletinRepository.save(bulletin);
        Long bulletinId = bulletin.getId();

        // WHEN THEN
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/bulletins/" + bulletinId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        assertFalse(bulletinRepository.findById(bulletinId).isPresent());
    }

    @Test
    @WithMockUser(username = "tresorier", roles = {"TRESORIER"})
    public void testDeleteBulletinAvecRoleTresorierRetourne403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/bulletins/1")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}
