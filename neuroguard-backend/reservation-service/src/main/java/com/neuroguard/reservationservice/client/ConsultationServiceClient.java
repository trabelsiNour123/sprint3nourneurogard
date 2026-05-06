package com.neuroguard.reservationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "consultation-service")
public interface ConsultationServiceClient {

    @PostMapping("/api/consultations/internal")
    Map<String, Object> createConsultation(@RequestBody Map<String, Object> request);
}
