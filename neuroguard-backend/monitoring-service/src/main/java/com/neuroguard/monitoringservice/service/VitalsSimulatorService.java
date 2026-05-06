package com.neuroguard.monitoringservice.service;

import com.neuroguard.monitoringservice.entity.VitalsEntity;
import com.neuroguard.monitoringservice.repository.VitalsRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class VitalsSimulatorService {

    private final VitalsRepository vitalsRepository;
    private final Random random = new Random();

    public VitalsSimulatorService(VitalsRepository vitalsRepository) {
        this.vitalsRepository = vitalsRepository;
    }

    @Scheduled(fixedRate = 120000)
    public void generateVitals() {
        int heartRate = random.nextInt(41) + 65; 
        int systolicBp = random.nextInt(26) + 110; 
        int diastolicBp = random.nextInt(16) + 70; 
        double temperature = 36.5 + (random.nextDouble() * 1.3); 

        temperature = Math.round(temperature * 10.0) / 10.0;

        int oxygenSaturation = random.nextInt(7) + 94; 

        String status = "normal";
        if (oxygenSaturation < 95 || temperature > 37.5) {
            status = "warning";
        }

        VitalsEntity vitals = new VitalsEntity(
                "2", // Mock patient ID
                heartRate,
                systolicBp,
                diastolicBp,
                temperature,
                oxygenSaturation,
                LocalDateTime.now(),
                status);

        vitalsRepository.save(vitals);

        System.out.println("Generated mocked vitals for Patient 2: " + status +
                " (HR: " + heartRate + ", BP: " + systolicBp + "/" + diastolicBp +
                ", Temp: " + temperature + ", SpO2: " + oxygenSaturation + "%)");
    }
}
