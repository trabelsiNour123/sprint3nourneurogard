package com.neuroguard.assuranceservice.client;

import com.neuroguard.assuranceservice.config.FeignClientConfig;
import com.neuroguard.assuranceservice.dto.MedicalHistoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "medical-history-service", configuration = FeignClientConfig.class)
public interface MedicalHistoryClient {

    @GetMapping("/api/patient/medical-history/{patientId}")
    MedicalHistoryDto getMedicalHistoryByPatientId(@PathVariable Long patientId);
}
