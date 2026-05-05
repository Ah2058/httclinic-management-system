package infrax.teama.submit_service.dto;

import lombok.Data;

@Data
public class AdminUpdateRequest {
    private String diagnosis;
    private String notes;
    private String requiredMedicine;
    private String status;  // new, viewed, done
}
