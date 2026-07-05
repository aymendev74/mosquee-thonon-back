package org.mosqueethonon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mosqueethonon.dto.auth.LoginRequestDto;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.repository.UtilisateurRepository;
import org.mosqueethonon.v1.controller.TestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestAuthentificationController extends TestController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @BeforeEach
    public void initContext() {
        utilisateurRepository.deleteAll();

        UtilisateurEntity utilisateur = new UtilisateurEntity();
        utilisateur.setUsername("jane.d");
        utilisateur.setPassword(bCryptPasswordEncoder.encode("correct-password"));
        utilisateur.setEmail("jane.d@example.com");
        utilisateur.setEnabled(true);
        utilisateur.setRoles(new ArrayList<>());
        utilisateurRepository.save(utilisateur);
    }

    @Test
    public void testLogin_WithValidCredentials_ShouldReturnOkAndPersistSession() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("jane.d");
        request.setPassword("correct-password");

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/profile")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jane.d"));
    }

    @Test
    public void testLogin_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("jane.d");
        request.setPassword("wrong-password");

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

}
