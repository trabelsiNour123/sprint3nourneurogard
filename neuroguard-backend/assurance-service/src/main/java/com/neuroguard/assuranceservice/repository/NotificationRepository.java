package com.neuroguard.assuranceservice.repository;

import com.neuroguard.assuranceservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<Notification> findByType(String type);

    List<Notification> findByStatus(String status);

    List<Notification> findByChannelOrderByCreatedAtDesc(String channel);
}
