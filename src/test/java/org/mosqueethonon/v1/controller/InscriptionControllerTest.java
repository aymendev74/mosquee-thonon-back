package org.mosqueethonon.v1.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mosqueethonon.configuration.security.context.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class InscriptionControllerTest extends ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "aymen", roles = {"ENSEIGNANT"})
    public void testFindInscriptionsByCriteriaNoRoleAdminReturn403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/inscriptions")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "aymen", roles = {"ADMIN"})
    public void testFindInscriptionsByCriteriaRoleAdminReturn200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/inscriptions")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}
