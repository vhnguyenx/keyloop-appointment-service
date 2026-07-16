package com.keyloop.scheduler.repository;

import com.keyloop.scheduler.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Query("SELECT a FROM Appointment a WHERE a.startTime < :endTime AND a.endTime > :startTime " +
           "AND a.status <> com.keyloop.scheduler.enums.AppointmentStatus.CANCELLED")
    List<Appointment> findOverlappingActiveAppointments(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT DISTINCT a FROM Appointment a " +
           "LEFT JOIN FETCH a.technician " +
           "LEFT JOIN FETCH a.serviceBay " +
           "ORDER BY a.startTime ASC")
    List<Appointment> findAllByOrderByStartTimeAsc();
}
