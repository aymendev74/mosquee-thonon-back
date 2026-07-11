package org.mosqueethonon.configuration.oauth;

import org.junit.jupiter.api.Test;
import org.mosqueethonon.configuration.security.ApplicationConfiguration;
import org.mosqueethonon.v1.controller.TestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestAuthorizationServerConfig extends TestController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    @Test
    public void testAuthorize_WhenUnauthenticated_ShouldRedirectToFrontendLoginPage() throws Exception {
        String redirectUri = applicationConfiguration.getLoginRedirectUri();

        mockMvc.perform(MockMvcRequestBuilders.get("/oauth2/authorize")
                        .accept(MediaType.TEXT_HTML)
                        .queryParam("response_type", "code")
                        .queryParam("client_id", "moth-react-app")
                        .queryParam("redirect_uri", redirectUri)
                        .queryParam("code_challenge", "E9melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM")
                        .queryParam("code_challenge_method", "S256"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", redirectUri));
    }

}
