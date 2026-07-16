package com.keyloop.scheduler.service.availability;

import com.keyloop.scheduler.entity.ServiceBay;
import com.keyloop.scheduler.entity.ServiceType;
import com.keyloop.scheduler.entity.Technician;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AvailabilityService {

    void validateCustomer(UUID customerId);

    void validateVehicle(UUID customerId, UUID vehicleId);

    int calculateTotalDuration(List<ServiceType> services);

    List<Technician> findQualifiedTechnicians(UUID dealershipId, List<ServiceType> services);

    List<Technician> findAvailableTechnicians(
        List<Technician> technicians,
        LocalDateTime startTime,
        LocalDateTime endTime
    );

    List<ServiceBay> findAvailableServiceBays(
        UUID dealershipId,
        LocalDateTime startTime,
        LocalDateTime endTime
    );
}
