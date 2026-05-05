package infrax.teama.auth_service.performance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Performance tests for authentication service
 * Validates response times and throughput under various loads
 */
@SpringBootTest
@ActiveProfiles("test")
class AuthServicePerformanceTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	@Timeout(value = 2, unit = TimeUnit.SECONDS)
	void testLoginResponseTimeUnder2Seconds() throws Exception {
		// Arrange
		String loginRequest = "{\"username\":\"admin\",\"password\":\"admin\"}";

		// Act & Assert
		long startTime = System.currentTimeMillis();

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginRequest))
				.andExpect(status().isOk());

		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;

		assertTrue(duration < 2000, "Login should complete within 2 seconds");
	}

	@Test
	@Timeout(value = 15, unit = TimeUnit.SECONDS)
	void testMultipleLoginsPerformance() throws Exception {
		// Arrange
		String loginRequest = "{\"username\":\"admin\",\"password\":\"admin\"}";
		int numberOfAttempts = 10;

		// Act & Assert
		long startTime = System.currentTimeMillis();

		for (int i = 0; i < numberOfAttempts; i++) {
			mockMvc.perform(post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(loginRequest))
					.andExpect(status().isOk());
		}

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		long averageTime = totalTime / numberOfAttempts;

		assertTrue(totalTime < 15000, "10 logins should complete within 15 seconds");
		assertTrue(averageTime < 1500, "Average login time should be less than 1500ms");
	}

	@Test
	void testLoginSuccessRateUnderLoad() throws Exception {
		// Arrange
		String loginRequest = "{\"username\":\"admin\",\"password\":\"admin\"}";
		int numberOfAttempts = 20;
		int successCount = 0;

		// Act & Assert
		for (int i = 0; i < numberOfAttempts; i++) {
			try {
				mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginRequest))
						.andExpect(status().isOk());
				successCount++;
			} catch (AssertionError e) {
				// Count failures
			}
		}

		double successRate = (double) successCount / numberOfAttempts * 100;
		assertTrue(successRate >= 95, "Login success rate should be at least 95%");
	}

	@Test
	@Timeout(value = 3, unit = TimeUnit.SECONDS)
	void testLoginFailurePerformance() throws Exception {
		// Arrange
		String loginRequest = "{\"username\":\"invaliduser\",\"password\":\"wrongpassword\"}";

		// Act & Assert
		long startTime = System.currentTimeMillis();

		for (int i = 0; i < 5; i++) {
			mockMvc.perform(post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(loginRequest))
					.andExpect(status().isUnauthorized());
		}

		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;

		assertTrue(duration < 3000, "5 failed logins should complete within 3 seconds");
	}

	@Test
	void testResponseTimeVariation() throws Exception {
		// Arrange
		String loginRequest = "{\"username\":\"admin\",\"password\":\"admin\"}";
		long[] responseTimes = new long[10];

		// Act
		for (int i = 0; i < 10; i++) {
			long startTime = System.currentTimeMillis();

			mockMvc.perform(post("/api/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(loginRequest))
					.andExpect(status().isOk());

			responseTimes[i] = System.currentTimeMillis() - startTime;
		}

		// Assert - Calculate statistics
		long minTime = java.util.Arrays.stream(responseTimes).min().orElse(Long.MAX_VALUE);
		long maxTime = java.util.Arrays.stream(responseTimes).max().orElse(0L);
		long avgTime = java.util.Arrays.stream(responseTimes).sum() / 10;

		double variance = calculateVariance(responseTimes, avgTime);
		double stdDev = Math.sqrt(variance);

		assertTrue(maxTime < 1000, "Max response time should be less than 1 second");
		assertTrue(stdDev < 200, "Response times should be consistent (stdDev < 200ms)");
	}

	@Test
	@Timeout(value = 20, unit = TimeUnit.SECONDS)
	void testThroughputUnder10Seconds() throws Exception {
		// Arrange
		String loginRequest = "{\"username\":\"admin\",\"password\":\"admin\"}";
		int successCount = 0;

		// Act & Assert
		long startTime = System.currentTimeMillis();

		while (System.currentTimeMillis() - startTime < 10000) {
			try {
				mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginRequest))
						.andExpect(status().isOk());
				successCount++;
			} catch (AssertionError e) {
				// Continue on failure
			}
		}

		// Should handle at least 10 requests per 10 seconds
		assertTrue(successCount >= 10, "Should handle at least 10 requests per 10 seconds");
	}

	private double calculateVariance(long[] values, long mean) {
		double variance = 0;
		for (long value : values) {
			variance += Math.pow(value - mean, 2);
		}
		return variance / values.length;
	}
}

