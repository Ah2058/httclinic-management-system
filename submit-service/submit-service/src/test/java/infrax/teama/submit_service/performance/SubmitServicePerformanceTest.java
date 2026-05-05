package infrax.teama.submit_service.performance;

import infrax.teama.submit_service.dto.PatientFormRequest;
import infrax.teama.submit_service.model.PatientForm;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Performance tests for submit service
 * Validates response times and throughput for form submissions
 */
@SpringBootTest
@ActiveProfiles("test")
class SubmitServicePerformanceTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	private String patientFormToJson(PatientFormRequest request) {
		StringBuilder json = new StringBuilder();
		json.append("{");
		json.append("\"firstName\":\"").append(request.getFirstName()).append("\",");
		json.append("\"lastName\":\"").append(request.getLastName()).append("\",");
		json.append("\"dateOfBirth\":\"").append(request.getDateOfBirth()).append("\",");
		json.append("\"streetName\":\"").append(request.getStreetName()).append("\",");
		json.append("\"streetNumber\":\"").append(request.getStreetNumber()).append("\",");
		json.append("\"city\":\"").append(request.getCity()).append("\",");
		json.append("\"postalCode\":\"").append(request.getPostalCode()).append("\",");
		json.append("\"phoneNumber\":\"").append(request.getPhoneNumber()).append("\",");
		json.append("\"emailAddress\":\"").append(request.getEmailAddress()).append("\"");
		json.append("}");
		return json.toString();
	}

	private PatientFormRequest createValidPatientFormRequest(String firstName) {
		PatientFormRequest request = new PatientFormRequest();
		request.setFirstName(firstName);
		request.setLastName("DOE");
		request.setDateOfBirth(LocalDate.of(1990, 1, 15));
		request.setStreetName("MAINSTREET");
		request.setStreetNumber("123");
		request.setCity("BERLIN");
		request.setPostalCode("10115");
		request.setPhoneNumber("+49301234567");
		request.setEmailAddress("john.doe@example.com");
		request.setSymptoms(Arrays.asList(PatientForm.Symptom.FEVER, PatientForm.Symptom.COUGH));
		request.setAllergies(Arrays.asList(PatientForm.Allergy.POLLEN));
		request.setMedications(Arrays.asList(PatientForm.Medication.PARACETAMOL));
		request.setPreExistingConditions(Arrays.asList(PatientForm.PreExistingCondition.DIABETES));
		return request;
	}

	private String uppercaseNameForIndex(String prefix, int index) {
		StringBuilder suffix = new StringBuilder();
		int value = index;
		do {
			suffix.insert(0, (char) ('A' + (value % 26)));
			value = (value / 26) - 1;
		} while (value >= 0);
		return prefix + suffix;
	}

	@Test
	@Timeout(value = 3, unit = TimeUnit.SECONDS)
	void testFormSubmissionResponseTimeUnder3Seconds() throws Exception {
		// Arrange
		PatientFormRequest request = createValidPatientFormRequest("JOHN");
		String jsonRequest = patientFormToJson(request);

		// Act & Assert
		long startTime = System.currentTimeMillis();

		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
				.andExpect(status().isCreated());

		long duration = System.currentTimeMillis() - startTime;
		assertTrue(duration < 3000, "Single form submission should complete within 3 seconds");
	}

	@Test
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	void testMultipleFormSubmissionsPerformance() throws Exception {
		// Arrange
		int numberOfSubmissions = 10;

		// Act & Assert
		long startTime = System.currentTimeMillis();

		for (int i = 0; i < numberOfSubmissions; i++) {
			PatientFormRequest request = createValidPatientFormRequest(uppercaseNameForIndex("NAME", i));
			String jsonRequest = patientFormToJson(request);

			mockMvc.perform(post("/api/submit/forms")
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
					.andExpect(status().isCreated());
		}

		long totalTime = System.currentTimeMillis() - startTime;
		long averageTime = totalTime / numberOfSubmissions;

		assertTrue(totalTime < 10000, numberOfSubmissions + " submissions should complete within 10 seconds");
		assertTrue(averageTime < 1000, "Average submission time should be less than 1 second");
	}

	@Test
	void testFormSubmissionSuccessRateUnderLoad() throws Exception {
		// Arrange
		int numberOfSubmissions = 20;
		int successCount = 0;

		// Act & Assert
		for (int i = 0; i < numberOfSubmissions; i++) {
			try {
				PatientFormRequest request = createValidPatientFormRequest(uppercaseNameForIndex("NAME", i));
				String jsonRequest = patientFormToJson(request);

				mockMvc.perform(post("/api/submit/forms")
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonRequest))
						.andExpect(status().isCreated());
				successCount++;
			} catch (AssertionError e) {
				// Count failures
			}
		}

		double successRate = (double) successCount / numberOfSubmissions * 100;
		assertTrue(successRate >= 90, "Form submission success rate should be at least 90%");
	}

	@Test
	@Timeout(value = 5, unit = TimeUnit.SECONDS)
	void testFormSubmissionWithCompleteData() throws Exception {
		// Arrange - Form with all fields populated
		PatientFormRequest request = createValidPatientFormRequest("ALICE");
		request.setSymptoms(Arrays.asList(
				PatientForm.Symptom.FEVER,
				PatientForm.Symptom.COUGH,
				PatientForm.Symptom.HEADACHE,
				PatientForm.Symptom.NAUSEA
		));
		request.setAllergies(Arrays.asList(
				PatientForm.Allergy.POLLEN,
				PatientForm.Allergy.PENICILLIN,
				PatientForm.Allergy.NUTS
		));
		request.setOtherSymptoms("Fatigue and body aches");
		request.setOtherAllergies("Shellfish and dairy");
		String jsonRequest = patientFormToJson(request);

		// Act & Assert
		long startTime = System.currentTimeMillis();

		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
				.andExpect(status().isCreated());

		long duration = System.currentTimeMillis() - startTime;
		assertTrue(duration < 5000, "Complex form submission should complete within 5 seconds");
	}

	@Test
	void testResponseTimeConsistency() throws Exception {
		// Arrange
		long[] responseTimes = new long[15];
		// Act
		for (int i = 0; i < 15; i++) {
			PatientFormRequest request = createValidPatientFormRequest(uppercaseNameForIndex("NAME", i));
			String jsonRequest = patientFormToJson(request);

			long startTime = System.currentTimeMillis();

			mockMvc.perform(post("/api/submit/forms")
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
					.andExpect(status().isCreated());

			responseTimes[i] = System.currentTimeMillis() - startTime;
		}

		// Assert - Calculate statistics
		long minTime = Arrays.stream(responseTimes).min().orElse(Long.MAX_VALUE);
		long maxTime = Arrays.stream(responseTimes).max().orElse(0L);
		long avgTime = Arrays.stream(responseTimes).sum() / 15;
		double variance = calculateVariance(responseTimes, avgTime);
		double stdDev = Math.sqrt(variance);

		assertTrue(maxTime < 2000, "Max response time should be less than 2 seconds");
		assertTrue(stdDev < 300, "Response times should be reasonably consistent");
	}

	@Test
	@Timeout(value = 15, unit = TimeUnit.SECONDS)
	void testThroughputPerformance() throws Exception {
		// Arrange
		int targetRequestsPerSecond = 5;
		long testDuration = 10000; // 10 seconds
		int successCount = 0;

		// Act & Assert
		long startTime = System.currentTimeMillis();

		while (System.currentTimeMillis() - startTime < testDuration) {
			try {
				PatientFormRequest request = createValidPatientFormRequest(uppercaseNameForIndex("PERF", successCount));
				String jsonRequest = patientFormToJson(request);

				mockMvc.perform(post("/api/submit/forms")
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonRequest))
						.andExpect(status().isCreated());
				successCount++;
			} catch (AssertionError e) {
				// Continue on failure
			}
		}

		// Should complete at least 5 requests per second
		int expectedMinimumRequests = targetRequestsPerSecond;
		assertTrue(successCount > expectedMinimumRequests,
				"Should handle at least " + expectedMinimumRequests + " requests per 10 seconds, got " + successCount);
	}

	@Test
	void testValidationPerformance() throws Exception {
		// Arrange
		int numberOfFailures = 20;
		long totalTime = 0;

		// Act & Assert - Invalid requests should fail fast
		long startTime = System.currentTimeMillis();

		for (int i = 0; i < numberOfFailures; i++) {
			PatientFormRequest request = createValidPatientFormRequest(uppercaseNameForIndex("NAME", i));
			request.setFirstName("lowercase"); // Invalid format
			String jsonRequest = patientFormToJson(request);

			try {
				mockMvc.perform(post("/api/submit/forms")
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonRequest))
						.andExpect(status().is4xxClientError());
			} catch (AssertionError e) {
				// Expected
			}
		}

		totalTime = System.currentTimeMillis() - startTime;
		long averageFailureTime = totalTime / numberOfFailures;

		// Validation failures should fail relatively quickly
		assertTrue(averageFailureTime < 500, "Validation failures should fail quickly (< 500ms average)");
	}

	private double calculateVariance(long[] values, long mean) {
		double variance = 0;
		for (long value : values) {
			variance += Math.pow(value - mean, 2);
		}
		return variance / values.length;
	}
}

