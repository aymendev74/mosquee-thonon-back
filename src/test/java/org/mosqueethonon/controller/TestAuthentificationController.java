package org.mosqueethonon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mosqueethonon.configuration.security.ApplicationConfiguration;
import org.mosqueethonon.dto.auth.LoginRequestDto;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.repository.UtilisateurRepository;
import org.mosqueethonon.v1.controller.TestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

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

    @Test
    public void testLogin_ThenAuthorize_ShouldReturnAuthorizationCodeWithoutReprompting() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("jane.d");
        request.setPassword("correct-password");

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        String redirectUri = applicationConfiguration.getLoginRedirectUri();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/oauth2/authorize")
                        .session(session)
                        .accept(MediaType.TEXT_HTML)
                        .queryParam("response_type", "code")
                        .queryParam("client_id", "moth-react-app")
                        .queryParam("redirect_uri", redirectUri)
                        .queryParam("code_challenge", "E9melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM")
                        .queryParam("code_challenge_method", "S256"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        assertTrue(location != null && location.startsWith(redirectUri),
                "Expected redirect to the registered redirect_uri, but was: " + location);
        assertTrue(location.contains("code="),
                "Expected an authorization code in the redirect (session should already be authenticated), but was: " + location);
    }

}
