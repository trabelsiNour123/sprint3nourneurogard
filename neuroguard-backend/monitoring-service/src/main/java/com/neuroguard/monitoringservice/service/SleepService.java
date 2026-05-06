package com.neuroguard.monitoringservice.service;

import com.neuroguard.monitoringservice.entity.SleepEntity;
import com.neuroguard.monitoringservice.repository.SleepRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SleepService {

    private final SleepRepository sleepRepository;

    public SleepService(SleepRepository sleepRepository) {
        this.sleepRepository = sleepRepository;
    }

    public SleepEntity logSleep(SleepEntity sleepEntity) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        boolean alreadyLogged = sleepRepository
                .findFirstByPatientIdAndTimestampBetween(sleepEntity.getPatientId(), startOfDay, endOfDay)
                .isPresent();

        if (alreadyLogged) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Sleep already logged for today");
        }

        return sleepRepository.save(sleepEntity);
    }

    public SleepEntity getLatestSleep(String userId) {
        List<SleepEntity> history = sleepRepository.findAllByPatientIdOrderByTimestampDesc(userId);
        return history.isEmpty() ? null : history.get(0);
    }

    public List<SleepEntity> getSleepHistory(String patientId) {
        return sleepRepository.findAllByPatientIdOrderByTimestampDesc(patientId);
    }

    public boolean hasSleepLoggedToday(String patientId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        return sleepRepository
                .findFirstByPatientIdAndTimestampBetween(patientId, startOfDay, endOfDay)
                .isPresent();
    }
}

