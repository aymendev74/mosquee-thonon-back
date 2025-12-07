package org.mosqueethonon.v1.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class TestInscriptionController extends TestController {

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
