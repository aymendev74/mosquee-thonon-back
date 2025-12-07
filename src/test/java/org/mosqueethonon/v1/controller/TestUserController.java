package org.mosqueethonon.v1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mosqueethonon.authentication.user.ChangePasswordRequest;
import org.mosqueethonon.entity.mail.MailingActivationUtilisateurEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.repository.MailingActivationUtilisateurRepository;
import org.mosqueethonon.repository.UtilisateurRepository;
import org.mosqueethonon.v1.dto.account.EnableAccountDto;
import org.mosqueethonon.v1.dto.user.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestUserController extends TestController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MailingActivationUtilisateurRepository mailingActivationUtilisateurRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    private UserDto testUser;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @BeforeEach
    public void initContext() {
        this.mailingActivationUtilisateurRepository.deleteAll();
        this.utilisateurRepository.deleteAll();

        UtilisateurEntity utilisateurEntity = new UtilisateurEntity();
        utilisateurEntity.setUsername("john.d");
        utilisateurEntity.setPassword(bCryptPasswordEncoder.encode("password"));
        utilisateurEntity.setEmail("john.d@example.com");
        this.utilisateurRepository.save(utilisateurEntity);

        MailingActivationUtilisateurEntity entity = new MailingActivationUtilisateurEntity();
        entity.setToken("valid-token");
        entity.setUsername("john.d");
        this.mailingActivationUtilisateurRepository.save(entity);
    }

    @BeforeEach
    public void setup() {
        testUser = new UserDto();
        testUser.setEmail("test@example.com");
        testUser.setNom("Doe");
        testUser.setPrenom("John");
        testUser.setUsername("john.d");
    }

    // Tests pour les endpoints publics
    @Test
    @WithAnonymousUser
    public void testEnableAccount_ShouldBePublic() throws Exception {
        EnableAccountDto enableAccountDto = new EnableAccountDto();
        enableAccountDto.setToken("valid-token");
        enableAccountDto.setUsername("john.d");
        enableAccountDto.setPassword("password");

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/users/enable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enableAccountDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void testGetAccountInformations_ShouldBePublic() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/users/informations")
                        .param("token", "valid-token"))
                .andExpect(status().isOk());
    }

    // Tests pour les endpoints authentifiés
    @Test
    @WithMockUser(username = "john.d")
    public void testChangePassword_ShouldBeAccessibleWithAuthentication() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("password");
        request.setNewPassword("newPassword");

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // Tests pour les endpoints admin
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUsers_ShouldBeAccessibleByAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateUser_ShouldBeAccessibleByAdmin() throws Exception {
        UtilisateurEntity utilisateurEntity = this.utilisateurRepository.findByUsername("john.d").orElseThrow(() -> new Exception("User john.d not found"));
        mockMvc.perform(MockMvcRequestBuilders.put("/v1/users/" + utilisateurEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteUser_ShouldBeAccessibleByAdmin() throws Exception {
        UtilisateurEntity utilisateurEntity = utilisateurRepository.findByUsername("john.d").orElseThrow(() -> new Exception("User john.d not found !"));
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/users/" + utilisateurEntity.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testResendActivationMail_ShouldBeAccessibleByAdmin() throws Exception {
        UtilisateurEntity utilisateurEntity = utilisateurRepository.findByUsername("john.d").orElseThrow(() -> new Exception("User john.d not found !"));
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/users/" + utilisateurEntity.getId() + "/activationMail"))
                .andExpect(status().isOk());
    }

    // Tests de sécurité - Accès non autorisé
    @Test
    @WithMockUser(roles = "USER")
    public void testGetUsers_ShouldBeForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testCreateUser_ShouldBeForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    public void testChangePassword_ShouldBeForbiddenForAnonymous() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

}