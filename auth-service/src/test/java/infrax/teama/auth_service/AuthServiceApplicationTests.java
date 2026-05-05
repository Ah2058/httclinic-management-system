package infrax.teama.auth_service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceApplicationTests {

	@Autowired
	private WebApplicationContext webApplicationContext;


	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void testSuccessfulLogin() throws Exception {
		String loginRequest = "{\"username\":\"admin\",\"password\":\"admin\"}";

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").exists())
				.andExpect(jsonPath("$.token").isNotEmpty());
	}

	@Test
	void testLoginWithInvalidCredentials() throws Exception {
		String loginRequest = "{\"username\":\"invaliduser\",\"password\":\"wrongpassword\"}";

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void testLoginWithEmptyUsername() throws Exception {
		String loginRequest = "{\"username\":\"\",\"password\":\"admin\"}";

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testLoginWithEmptyPassword() throws Exception {
		String loginRequest = "{\"username\":\"admin\",\"password\":\"\"}";

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testLoginResponseFormatValidation() throws Exception {
		String loginRequest = "{\"username\":\"admin\",\"password\":\"admin\"}";

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.token").isString())
				.andExpect(jsonPath("$.token").value(notNullValue()));
	}

	@Test
	void testLoginRequestContentTypeValidation() throws Exception {
		String loginRequest = "{\"username\":\"admin\",\"password\":\"admin\"}";

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}

	@Test
	void testMultipleSequentialLogins() throws Exception {
		String loginRequest = "{\"username\":\"admin\",\"password\":\"admin\"}";

		// First login
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").exists());

		// Second login
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").exists());
	}

	@Test
	void testLoginWithNullFields() throws Exception {
		String loginRequest = "{\"username\":null,\"password\":\"admin\"}";

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testLoginWithMalformedJson() throws Exception {
		String loginRequest = "{\"username\":\"admin\",\"password\":\"admin\""; // Missing closing bracket

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testLoginWithSpecialCharactersInPassword() throws Exception {
		String loginRequest = "{\"username\":\"admin\",\"password\":\"admin!@#$%\"}";

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().isUnauthorized());
	}

}
