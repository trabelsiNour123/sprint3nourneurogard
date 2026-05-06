package com.neuroguard.assuranceservice.client;

import com.neuroguard.assuranceservice.config.FeignClientConfig;
import com.neuroguard.assuranceservice.dto.AlertDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "risk-alert-service", configuration = FeignClientConfig.class)
public interface RiskAlertClient {

    @GetMapping("/api/patient/{patientId}/alerts")
    List<AlertDto> getPatientAlerts(@PathVariable Long patientId);
}
