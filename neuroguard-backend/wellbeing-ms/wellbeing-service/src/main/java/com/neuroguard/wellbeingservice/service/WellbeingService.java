package com.neuroguard.wellbeingservice.service;

import com.neuroguard.wellbeingservice.dto.PatientPulseDTO;
import com.neuroguard.wellbeingservice.entity.Hydration;
import com.neuroguard.wellbeingservice.entity.Mood;
import com.neuroguard.wellbeingservice.entity.Sleep;
import com.neuroguard.wellbeingservice.repository.HydrationRepository;
import com.neuroguard.wellbeingservice.repository.MoodRepository;
import com.neuroguard.wellbeingservice.repository.SleepRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class WellbeingService {

    private final MoodRepository moodRepository;
    private final SleepRepository sleepRepository;
    private final HydrationRepository hydrationRepository;

    public WellbeingService(MoodRepository moodRepository, SleepRepository sleepRepository,
            HydrationRepository hydrationRepository) {
        this.moodRepository = moodRepository;
        this.sleepRepository = sleepRepository;
        this.hydrationRepository = hydrationRepository;
    }

    public Mood saveMood(Mood mood) {
        return moodRepository.save(mood);
    }

    public List<Mood> getMoodTrends(String userId) {
        return moodRepository.findByUserIdOrderByTimestampDesc(userId, PageRequest.of(0, 7));
    }

    public Sleep logSleep(Sleep sleep) {
        return sleepRepository.save(sleep);
    }

    public Double getAverageSleep(String userId) {
        return sleepRepository.findAll().stream()
                .filter(s -> s.getUserId().equals(userId))
                .limit(7)
                .mapToDouble(Sleep::getHours)
                .average()
                .orElse(0.0);
    }

    public Hydration addHydration(String userId) {
        LocalDate today = LocalDate.now();
        Hydration hydration = hydrationRepository.findByUserIdAndDate(userId, today)
                .orElse(new Hydration(null, userId, 0, 8, today));
        hydration.setGlassesCount(hydration.getGlassesCount() + 1);
        return hydrationRepository.save(hydration);
    }

    public Hydration getTodayHydration(String userId) {
        return hydrationRepository.findByUserIdAndDate(userId, LocalDate.now())
                .orElse(new Hydration(null, userId, 0, 8, LocalDate.now()));
    }

    public PatientPulseDTO getPatientPulse(String userId) {
        Optional<Mood> latestMood = moodRepository.findByUserIdOrderByTimestampDesc(userId, PageRequest.of(0, 1))
                .stream().findFirst();
        Optional<Sleep> latestSleep = sleepRepository.findTopByUserIdOrderByDateDesc(userId);
        Optional<Hydration> todayHydration = hydrationRepository.findByUserIdAndDate(userId, LocalDate.now());

        String status = "stable";

        if (todayHydration.isPresent()
                && (todayHydration.get().getGlassesCount() * 100.0 / todayHydration.get().getTargetGlasses()) < 60.0) {
            status = "attention";
        } else if (latestSleep.isPresent() && latestSleep.get().getHours() < 6.0) {
            status = "monitor";
        }

        PatientPulseDTO pulse = new PatientPulseDTO();
        pulse.setMoodValue(latestMood.map(m -> m.getMoodLabel() + " " + m.getEmoji()).orElse("N/A"));
        pulse.setSleepValue(latestSleep.map(s -> s.getHours() + "h (" + s.getQuality() + ")").orElse("N/A"));
        pulse.setHydrationValue(
                todayHydration.map(h -> (int) (h.getGlassesCount() * 100.0 / h.getTargetGlasses()) + "%").orElse("0%"));
        pulse.setStatus(status);

        return pulse;
    }

    public Hydration resetHydration(String userId) {
        // Logic to find today's hydration record and set glassesCount to 0
        Hydration hydration = getTodayHydration(userId);
        hydration.setGlassesCount(0);
        return hydrationRepository.save(hydration);
    }
    public Mood getLatestMood(String userId) {
        // Example: find Top 1 by userId order by date descending
        return moodRepository.findFirstByUserIdOrderByTimestampDesc(userId).orElse(null);
    }
    public Sleep getLatestSleep(String userId) {
        // Finds the most recent sleep record for the user
        return sleepRepository.findTopByUserIdOrderByDateDesc(userId).orElse(null);
    }



}
