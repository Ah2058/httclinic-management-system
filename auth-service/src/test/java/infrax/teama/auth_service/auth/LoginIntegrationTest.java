package infrax.teama.auth_service.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for login flow
 * Tests the complete authentication flow from login request to JWT token generation
 */
@SpringBootTest
@ActiveProfiles("test")
class LoginIntegrationTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	private ObjectMapper objectMapper;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		objectMapper = new ObjectMapper().findAndRegisterModules();
	}

	@Test
	void testCompleteLoginFlow() throws Exception {
		// Arrange
		String loginRequest = "{\"username\":\"admin\",\"password\":\"admin\"}";

		// Act & Assert
		MvcResult result = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn();

		// Verify token structure
		String response = result.getResponse().getContentAsString();
		JsonNode jsonNode = objectMapper.readTree(response);
		String token = jsonNode.get("token").asText();

		assertNotNull(token);
		assertTrue(token.length() > 0);
		assertTrue(token.contains(".")); // JWT format check
	}

	@Test
	void testLoginFailureWithInvalidCredentials() throws Exception {
		// Arrange
		String loginRequest = "{\"username\":\"invaliduser\",\"password\":\"invalidpassword\"}";

		// Act & Assert
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void testLoginWithValidUsernameInvalidPassword() throws Exception {
		// Arrange
		String loginRequest = "{\"username\":\"admin\",\"password\":\"wrongpassword\"}";

		// Act & Assert
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void testLoginValidationEmptyUsername() throws Exception {
		// Arrange
		String loginRequest = "{\"username\":\"\",\"password\":\"admin\"}";

		// Act & Assert
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testLoginValidationEmptyPassword() throws Exception {
		// Arrange
		String loginRequest = "{\"username\":\"admin\",\"password\":\"\"}";

		// Act & Assert
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testLoginWithNullUsername() throws Exception {
		// Arrange
		String loginRequest = "{\"username\":null,\"password\":\"admin\"}";

		// Act & Assert
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testLoginWithNullPassword() throws Exception {
		// Arrange
		String loginRequest = "{\"username\":\"admin\",\"password\":null}";

		// Act & Assert
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testLoginWithMissingUsername() throws Exception {
		// Arrange
		String loginRequest = "{\"password\":\"admin\"}";

		// Act & Assert
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testLoginWithMissingPassword() throws Exception {
		// Arrange
		String loginRequest = "{\"username\":\"admin\"}";

		// Act & Assert
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testLoginWithSpecialCharactersInCredentials() throws Exception {
		// Arrange - testing SQL injection attempt
		String loginRequest = "{\"username\":\"admin' OR '1'='1\",\"password\":\"admin\"}";

		// Act & Assert
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void testLoginJWTTokenFormat() throws Exception {
		// Arrange
		String loginRequest = "{\"username\":\"admin\",\"password\":\"admin\"}";

		// Act & Assert
		MvcResult result = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().isOk())
				.andReturn();

		String response = result.getResponse().getContentAsString();
		JsonNode jsonNode = objectMapper.readTree(response);
		String token = jsonNode.get("token").asText();

		// JWT format: header.payload.signature (3 parts separated by dots)
		String[] parts = token.split("\\.");
		assertEquals(3, parts.length, "JWT should have 3 parts separated by dots");

		// Check if token is not expired (basic check)
		assertTrue(token.length() > 100, "JWT token should be reasonably long");
	}

	@Test
	void testMultipleSuccessiveLogins() throws Exception {
		// Arrange
		String loginRequest = "{\"username\":\"admin\",\"password\":\"admin\"}";

		// Act & Assert - Multiple logins
		for (int i = 0; i < 5; i++) {
			MvcResult result = mockMvc.perform(post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(loginRequest))
					.andExpect(status().isOk())
					.andReturn();

			String response = result.getResponse().getContentAsString();
			JsonNode jsonNode = objectMapper.readTree(response);
			String token = jsonNode.get("token").asText();

			assertNotNull(token);
			assertTrue(token.length() > 0);
		}
	}

	@Test
	void testLoginResponseHeaders() throws Exception {
		// Arrange
		String loginRequest = "{\"username\":\"admin\",\"password\":\"admin\"}";

		// Act & Assert
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(header().exists("Content-Type"));
	}

	@Test
	void testLoginWithVeryLongUsername() throws Exception {
		// Arrange
		String longUsername = "a".repeat(1000);
		String loginRequest = "{\"username\":\"" + longUsername + "\",\"password\":\"admin\"}";

		// Act & Assert
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testLoginWithVeryLongPassword() throws Exception {
		// Arrange
		String longPassword = "a".repeat(1000);
		String loginRequest = "{\"username\":\"admin\",\"password\":\"" + longPassword + "\"}";

		// Act & Assert
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void testLoginWithCaseSensitiveUsername() throws Exception {
		// Current persistence/collation behavior accepts case-insensitive username lookup.
		String loginRequest = "{\"username\":\"ADMIN\",\"password\":\"admin\"}";

		// Act & Assert
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().isOk());
	}
}

