package infrax.teama.auth_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthControllerIntegrationTest {
    private static final String VALID_USERNAME = "admin";
    private static final String VALID_PASSWORD = "admin";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;


    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testLoginEndpointWithValidCredentials() throws Exception {
        String loginRequest = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", VALID_USERNAME, VALID_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String token = result.getResponse().getContentAsString();
        assert !token.isEmpty();
    }

    @Test
    void testLoginEndpointWithInvalidCredentials() throws Exception {
        String loginRequest = "{\"username\":\"invaliduser\",\"password\":\"wrongpassword\"}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginEndpointWithEmptyUsername() throws Exception {
        String loginRequest = String.format("{\"username\":\"\",\"password\":\"%s\"}", VALID_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testLoginEndpointWithEmptyPassword() throws Exception {
        String loginRequest = String.format("{\"username\":\"%s\",\"password\":\"\"}", VALID_USERNAME);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testLoginEndpointWithMissingFields() throws Exception {
        String loginRequest = String.format("{\"username\":\"%s\"}", VALID_USERNAME);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testLoginEndpointReturnsValidJWT() throws Exception {
        String loginRequest = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", VALID_USERNAME, VALID_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assert responseBody.contains("token");
        assert responseBody.contains(".");  // JWT should have dots
    }

    @Test
    void testLoginEndpointContentTypeValidation() throws Exception {
        String loginRequest = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", VALID_USERNAME, VALID_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testMultipleLoginAttemptsWithDifferentCredentials() throws Exception {
        // First attempt - invalid
        String invalidRequest = "{\"username\":\"invalid\",\"password\":\"invalid\"}";
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isUnauthorized());

        // Second attempt - valid
        String validRequest = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", VALID_USERNAME, VALID_PASSWORD);
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void testLoginEndpointResponseFormat() throws Exception {
        String loginRequest = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", VALID_USERNAME, VALID_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}

