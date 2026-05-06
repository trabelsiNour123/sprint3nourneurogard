package com.neuroguard.assuranceservice.client;

import com.neuroguard.assuranceservice.config.FeignClientConfig;
import com.neuroguard.assuranceservice.dto.MLPredictionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "ml-predictor-service", url = "${ml-predictor.service.url:http://localhost:5000}", configuration = FeignClientConfig.class)
public interface MLPredictorClient {

    @PostMapping("/predict")
    MLPredictionDto predict(@RequestBody Map<String, Object> features);
}
