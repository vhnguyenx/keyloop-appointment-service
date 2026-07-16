package com.keyloop.scheduler.dto.response;

import com.keyloop.scheduler.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {

    private UUID appointmentId;
    private AppointmentStatus status;
    private UUID technicianId;
    private UUID serviceBayId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
