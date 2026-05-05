package infrax.teama.submit_service.integration;

import infrax.teama.submit_service.dto.PatientFormRequest;
import infrax.teama.submit_service.model.PatientForm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-End Integration Tests for Submit Service
 * Tests complete workflows for patient form submission and retrieval
 */
@SpringBootTest
@ActiveProfiles("test")
class SubmitServiceE2ETest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	private ObjectMapper objectMapper;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		objectMapper = new ObjectMapper().findAndRegisterModules();
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
		json.append("\"emailAddress\":").append(request.getEmailAddress() == null ? "null" : "\"" + request.getEmailAddress() + "\"").append(",");
		json.append("\"otherSymptoms\":").append(request.getOtherSymptoms() == null ? "null" : "\"" + request.getOtherSymptoms() + "\"").append(",");
		json.append("\"otherAllergies\":").append(request.getOtherAllergies() == null ? "null" : "\"" + request.getOtherAllergies() + "\"").append(",");
		json.append("\"otherMedications\":").append(request.getOtherMedications() == null ? "null" : "\"" + request.getOtherMedications() + "\"").append(",");
		json.append("\"otherPreExistingConditions\":").append(request.getOtherPreExistingConditions() == null ? "null" : "\"" + request.getOtherPreExistingConditions() + "\"").append(",");
		json.append("\"symptoms\":").append(enumListToJson(request.getSymptoms())).append(",");
		json.append("\"allergies\":").append(enumListToJson(request.getAllergies())).append(",");
		json.append("\"medications\":").append(enumListToJson(request.getMedications())).append(",");
		json.append("\"preExistingConditions\":").append(enumListToJson(request.getPreExistingConditions()));
		json.append("}");
		return json.toString();
	}

	private String enumListToJson(java.util.List<? extends Enum<?>> values) {
		if (values == null) {
			return "null";
		}
		return values.stream()
				.map(Enum::name)
				.map(value -> "\"" + value + "\"")
				.collect(java.util.stream.Collectors.joining(",", "[", "]"));
	}

	private PatientFormRequest createValidPatientFormRequest() {
		PatientFormRequest request = new PatientFormRequest();
		request.setFirstName("JOHN");
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

	@Test
	void testCompletePatientFormSubmissionFlow() throws Exception {
		// Arrange
		PatientFormRequest request = createValidPatientFormRequest();
		String jsonRequest = patientFormToJson(request);

		// Act - Submit form
		MvcResult submitResult = mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
				.andExpect(status().isCreated())
				.andReturn();

		// Assert - Verify response
		String response = submitResult.getResponse().getContentAsString();
		JsonNode jsonNode = objectMapper.readTree(response);
		Long formId = jsonNode.get("id").asLong();
		String status = jsonNode.get("status").asText();

		assertNotNull(formId);
		assertTrue(formId > 0);
		assertEquals("new", status);

		// Verify the submitted data
		assertEquals("JOHN", jsonNode.get("firstName").asText());
		assertEquals("DOE", jsonNode.get("lastName").asText());
	}

	@Test
	void testPatientFormDataPersistence() throws Exception {
		// Arrange
		PatientFormRequest request = createValidPatientFormRequest();
		request.setFirstName("JANE");
		request.setOtherSymptoms("Fatigue and weakness");
		String jsonRequest = patientFormToJson(request);

		// Act - Submit form
		MvcResult submitResult = mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
				.andExpect(status().isCreated())
				.andReturn();

		// Assert - All data should be returned correctly
		String response = submitResult.getResponse().getContentAsString();
		JsonNode jsonNode = objectMapper.readTree(response);

		assertEquals("JANE", jsonNode.get("firstName").asText());
		assertEquals("Fatigue and weakness", jsonNode.get("otherSymptoms").asText());
		assertEquals("new", jsonNode.get("status").asText());
	}

	@Test
	void testMultipleFormsSubmissionSequence() throws Exception {
		// Arrange
		int numberOfForms = 3;
		Long[] formIds = new Long[numberOfForms];

		// Act & Assert - Submit multiple forms
		for (int i = 0; i < numberOfForms; i++) {
			PatientFormRequest request = createValidPatientFormRequest();
			request.setFirstName("NAME" + (char) ('A' + i));
			String jsonRequest = patientFormToJson(request);

			MvcResult result = mockMvc.perform(post("/api/submit/forms")
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
					.andExpect(status().isCreated())
					.andReturn();

			String response = result.getResponse().getContentAsString();
			JsonNode jsonNode = objectMapper.readTree(response);
			formIds[i] = jsonNode.get("id").asLong();

			assertNotNull(formIds[i]);
		}

		// Verify all IDs are unique
		for (int i = 0; i < numberOfForms - 1; i++) {
			assertNotEquals(formIds[i], formIds[i + 1], "Form IDs should be unique");
		}
	}

	@Test
	void testFormSubmissionWithCompleteValidation() throws Exception {
		// Arrange
		PatientFormRequest request = createValidPatientFormRequest();
		request.setSymptoms(Arrays.asList(
				PatientForm.Symptom.FEVER,
				PatientForm.Symptom.COUGH,
				PatientForm.Symptom.HEADACHE
		));
		request.setAllergies(Arrays.asList(
				PatientForm.Allergy.POLLEN,
				PatientForm.Allergy.PENICILLIN,
				PatientForm.Allergy.NUTS
		));
		request.setMedications(Arrays.asList(
				PatientForm.Medication.PARACETAMOL,
				PatientForm.Medication.IBUPROFEN
		));
		request.setPreExistingConditions(Arrays.asList(
				PatientForm.PreExistingCondition.DIABETES,
				PatientForm.PreExistingCondition.ASTHMA
		));
		String jsonRequest = patientFormToJson(request);

		// Act
		MvcResult result = mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
				.andExpect(status().isCreated())
				.andReturn();

		// Assert
		String response = result.getResponse().getContentAsString();
		JsonNode jsonNode = objectMapper.readTree(response);

		assertEquals("JOHN", jsonNode.get("firstName").asText());
		assertNotNull(jsonNode.get("id"));
		assertTrue(jsonNode.get("id").asLong() > 0);
	}

	@Test
	void testFormSubmissionWithValidationFailures() throws Exception {
		// Test multiple validation scenarios

		// Test 1: Lowercase first name
		PatientFormRequest invalidRequest1 = createValidPatientFormRequest();
		invalidRequest1.setFirstName("john");
		String json1 = patientFormToJson(invalidRequest1);

		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json1))
				.andExpect(status().is4xxClientError());

		// Test 2: Invalid phone number
		PatientFormRequest invalidRequest2 = createValidPatientFormRequest();
		invalidRequest2.setPhoneNumber("invalid");
		String json2 = patientFormToJson(invalidRequest2);

		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json2))
				.andExpect(status().is4xxClientError());

		// Test 3: Invalid email
		PatientFormRequest invalidRequest3 = createValidPatientFormRequest();
		invalidRequest3.setEmailAddress("not-an-email");
		String json3 = patientFormToJson(invalidRequest3);

		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json3))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testFormSubmissionResponseStructure() throws Exception {
		// Arrange
		PatientFormRequest request = createValidPatientFormRequest();
		String jsonRequest = patientFormToJson(request);

		// Act
		MvcResult result = mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn();

		// Assert - Check response structure
		String response = result.getResponse().getContentAsString();
		JsonNode jsonNode = objectMapper.readTree(response);

		// Verify all essential fields exist
		assertTrue(jsonNode.has("id"));
		assertTrue(jsonNode.has("firstName"));
		assertTrue(jsonNode.has("lastName"));
		assertTrue(jsonNode.has("dateOfBirth"));
		assertTrue(jsonNode.has("status"));

		// Verify field types
		assertTrue(jsonNode.get("id").isNumber());
		assertTrue(jsonNode.get("firstName").isTextual());
		assertTrue(jsonNode.get("lastName").isTextual());
		assertTrue(jsonNode.get("status").isTextual());
	}

	@Test
	void testFormSubmissionWithEmptyOptionalFields() throws Exception {
		// Arrange
		PatientFormRequest request = createValidPatientFormRequest();
		request.setOtherSymptoms(null);
		request.setOtherAllergies(null);
		request.setOtherMedications(null);
		request.setOtherPreExistingConditions(null);
		String jsonRequest = patientFormToJson(request);

		// Act & Assert
		MvcResult result = mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
				.andExpect(status().isCreated())
				.andReturn();

		String response = result.getResponse().getContentAsString();
		JsonNode jsonNode = objectMapper.readTree(response);

		assertNotNull(jsonNode.get("id"));
		assertTrue(jsonNode.get("id").asLong() > 0);
	}

	@Test
	void testLargeFormSubmission() throws Exception {
		// Arrange - Create a form with maximum data
		PatientFormRequest request = createValidPatientFormRequest();
		request.setOtherSymptoms("Very long symptom description ".repeat(5));
		request.setOtherAllergies("Very long allergy description ".repeat(5));
		request.setOtherMedications("Very long medication description ".repeat(5));
		request.setOtherPreExistingConditions("Very long pre-existing condition description ".repeat(3));
		String jsonRequest = patientFormToJson(request);

		// Act & Assert
		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber());
	}

	@Test
	void testFormSubmissionContentTypeValidation() throws Exception {
		// Arrange
		PatientFormRequest request = createValidPatientFormRequest();
		String jsonRequest = patientFormToJson(request);

		// Act & Assert - Verify content type header
		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}

	@Test
	void testFormSubmissionWithBoundaryDates() throws Exception {
		// Test with various birth dates

		// Test 1: Very old birth date
		PatientFormRequest request1 = createValidPatientFormRequest();
		request1.setDateOfBirth(LocalDate.of(1920, 1, 1));
		String json1 = patientFormToJson(request1);

		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json1))
				.andExpect(status().isCreated());

		// Test 2: Recent birth date
		PatientFormRequest request2 = createValidPatientFormRequest();
		request2.setDateOfBirth(LocalDate.now().minusYears(1));
		String json2 = patientFormToJson(request2);
		
		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json2))
				.andExpect(status().isCreated());
	}
}

