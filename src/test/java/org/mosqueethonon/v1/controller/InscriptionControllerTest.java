package org.mosqueethonon.v1.controller;

import org.junit.jupiter.api.Test;
import org.mosqueethonon.authentication.jwt.JwtTokenUtil;
import org.mosqueethonon.entity.UtilisateurEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class InscriptionControllerTest extends ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testFindInscriptionsByCriteria() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/inscriptions")
                        .header("Authorization", generateToken()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}
