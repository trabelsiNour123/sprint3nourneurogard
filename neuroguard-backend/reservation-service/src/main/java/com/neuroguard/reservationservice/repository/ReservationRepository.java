package com.neuroguard.reservationservice.repository;

import com.neuroguard.reservationservice.entity.Reservation;
import com.neuroguard.reservationservice.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByPatientIdOrderByReservationDateDesc(Long patientId);
    List<Reservation> findByProviderIdOrderByReservationDateDesc(Long providerId);
    List<Reservation> findByProviderIdAndStatusOrderByReservationDateDesc(Long providerId, ReservationStatus status);
    List<Reservation> findByPatientIdAndStatusOrderByReservationDateDesc(Long patientId, ReservationStatus status);
    List<Reservation> findByProviderIdAndReservationDateBetween(Long providerId, LocalDateTime startDate, LocalDateTime endDate);
    boolean existsByProviderIdAndReservationDateAndTimeSlotAndStatusIn(Long providerId, LocalDateTime reservationDate, java.time.LocalTime timeSlot, List<ReservationStatus> statuses);
    @org.springframework.data.jpa.repository.Query("SELECT r.status, COUNT(r) FROM Reservation r GROUP BY r.status")
    List<Object[]> countByStatus();

    @org.springframework.data.jpa.repository.Query("SELECT r.patientId, COUNT(r) FROM Reservation r GROUP BY r.patientId ORDER BY COUNT(r) DESC")
    List<Object[]> findTopPatientIds();
}
