package infrax.teama.submit_service;

import infrax.teama.submit_service.dto.PatientFormRequest;
import infrax.teama.submit_service.model.PatientForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
class SubmitServiceApplicationTests {

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

	private String toJson(PatientFormRequest request) {
		return """
				{
				  "firstName": %s,
				  "lastName": %s,
				  "dateOfBirth": %s,
				  "streetName": %s,
				  "streetNumber": %s,
				  "city": %s,
				  "postalCode": %s,
				  "phoneNumber": %s,
				  "emailAddress": %s,
				  "symptoms": %s,
				  "otherSymptoms": %s,
				  "allergies": %s,
				  "otherAllergies": %s,
				  "medications": %s,
				  "otherMedications": %s,
				  "preExistingConditions": %s,
				  "otherPreExistingConditions": %s
				}
				""".formatted(
				jsonString(request.getFirstName()),
				jsonString(request.getLastName()),
				jsonString(request.getDateOfBirth() != null ? request.getDateOfBirth().toString() : null),
				jsonString(request.getStreetName()),
				jsonString(request.getStreetNumber()),
				jsonString(request.getCity()),
				jsonString(request.getPostalCode()),
				jsonString(request.getPhoneNumber()),
				jsonString(request.getEmailAddress()),
				jsonEnumList(request.getSymptoms()),
				jsonString(request.getOtherSymptoms()),
				jsonEnumList(request.getAllergies()),
				jsonString(request.getOtherAllergies()),
				jsonEnumList(request.getMedications()),
				jsonString(request.getOtherMedications()),
				jsonEnumList(request.getPreExistingConditions()),
				jsonString(request.getOtherPreExistingConditions()));
	}

	private String jsonEnumList(List<? extends Enum<?>> values) {
		if (values == null) {
			return "null";
		}
		return values.stream()
				.map(Enum::name)
				.map(this::jsonString)
				.collect(java.util.stream.Collectors.joining(", ", "[", "]"));
	}

	private String jsonString(String value) {
		if (value == null) {
			return "null";
		}
		return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}

	@Test
	void testSubmitPatientFormWithValidData() throws Exception {
		PatientFormRequest request = createValidPatientFormRequest();
		String jsonRequest = toJson(request);

		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.firstName").value("JOHN"))
				.andExpect(jsonPath("$.lastName").value("DOE"));
	}

	@Test
	void testSubmitPatientFormWithMissingFirstName() throws Exception {
		PatientFormRequest request = createValidPatientFormRequest();
		request.setFirstName(null);
		String jsonRequest = toJson(request);

		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testSubmitPatientFormWithMissingLastName() throws Exception {
		PatientFormRequest request = createValidPatientFormRequest();
		request.setLastName(null);
		String jsonRequest = toJson(request);

		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testSubmitPatientFormWithInvalidPhoneNumber() throws Exception {
		PatientFormRequest request = createValidPatientFormRequest();
		request.setPhoneNumber("invalid");
		String jsonRequest = toJson(request);

		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testSubmitPatientFormWithInvalidEmail() throws Exception {
		PatientFormRequest request = createValidPatientFormRequest();
		request.setEmailAddress("not-an-email");
		String jsonRequest = toJson(request);

		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testSubmitPatientFormWithLowercaseFirstName() throws Exception {
		PatientFormRequest request = createValidPatientFormRequest();
		request.setFirstName("john"); // lowercase
		String jsonRequest = toJson(request);

		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testSubmitPatientFormWithNonNumericStreetNumber() throws Exception {
		PatientFormRequest request = createValidPatientFormRequest();
		request.setStreetNumber("ABC");
		String jsonRequest = toJson(request);

		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void testSubmitMultiplePatientFormsSequentially() throws Exception {
		PatientFormRequest request1 = createValidPatientFormRequest();
		request1.setFirstName("JOHN");
		String json1 = toJson(request1);

		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json1))
				.andExpect(status().isCreated());

		PatientFormRequest request2 = createValidPatientFormRequest();
		request2.setFirstName("JANE");
		String json2 = toJson(request2);

		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json2))
				.andExpect(status().isCreated());
	}

	@Test
	void testSubmitPatientFormResponseFormat() throws Exception {
		PatientFormRequest request = createValidPatientFormRequest();
		String jsonRequest = toJson(request);

		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").isMap())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.status").value("new"));
	}

	@Test
	void testSubmitPatientFormWithAllMedicalData() throws Exception {
		PatientFormRequest request = createValidPatientFormRequest();
		request.setSymptoms(Arrays.asList(PatientForm.Symptom.FEVER, PatientForm.Symptom.COUGH, PatientForm.Symptom.HEADACHE));
		request.setAllergies(Arrays.asList(PatientForm.Allergy.POLLEN, PatientForm.Allergy.PENICILLIN));
		request.setMedications(Arrays.asList(PatientForm.Medication.PARACETAMOL, PatientForm.Medication.IBUPROFEN));
		request.setPreExistingConditions(Arrays.asList(PatientForm.PreExistingCondition.DIABETES, PatientForm.PreExistingCondition.ASTHMA));
		request.setOtherSymptoms("Fatigue");
		request.setOtherAllergies("Shellfish");
		request.setOtherMedications("Vitamin D");
		request.setOtherPreExistingConditions("Food sensitivity");
		String jsonRequest = toJson(request);

		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber());
	}

	@Test
	void testSubmitPatientFormWithMissingMedicalData() throws Exception {
		PatientFormRequest request = createValidPatientFormRequest();
		request.setSymptoms(null);
		request.setAllergies(null);
		request.setMedications(null);
		request.setPreExistingConditions(null);
		String jsonRequest = toJson(request);

		mockMvc.perform(post("/api/submit/forms")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
				.andExpect(status().isCreated());
	}

}
