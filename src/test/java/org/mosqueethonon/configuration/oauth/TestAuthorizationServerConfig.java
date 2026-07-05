package org.mosqueethonon.configuration.oauth;

import org.junit.jupiter.api.Test;
import org.mosqueethonon.configuration.security.ApplicationConfiguration;
import org.mosqueethonon.v1.controller.TestController;
import org.springframework.beans.factory.annotation.Autowired;
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
        mockMvc.perform(MockMvcRequestBuilders.get("/oauth2/authorize"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", applicationConfiguration.getLoginRedirectUri()));
    }

}
