package com.keyloop.scheduler.service.impl;

import com.keyloop.scheduler.entity.Appointment;
import com.keyloop.scheduler.entity.ServiceBay;
import com.keyloop.scheduler.entity.ServiceType;
import com.keyloop.scheduler.entity.Technician;
import com.keyloop.scheduler.exception.InvalidAppointmentException;
import com.keyloop.scheduler.exception.ResourceNotFoundException;
import com.keyloop.scheduler.repository.AppointmentRepository;
import com.keyloop.scheduler.repository.CustomerRepository;
import com.keyloop.scheduler.repository.ServiceBayRepository;
import com.keyloop.scheduler.repository.TechnicianRepository;
import com.keyloop.scheduler.repository.VehicleRepository;
import com.keyloop.scheduler.service.availability.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvailabilityServiceImpl implements AvailabilityService {

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final TechnicianRepository technicianRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceBayRepository serviceBayRepository;

    @Override
    public void validateCustomer(UUID customerId) {
        if (customerId == null || !customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer with ID " + customerId + " not found");
        }
    }

    @Override
    public void validateVehicle(UUID customerId, UUID vehicleId) {
        if (vehicleId == null) {
            throw new ResourceNotFoundException("Vehicle ID must not be null");
        }
        var vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle with ID " + vehicleId + " not found"));

        if (customerId == null || vehicle.getCustomer() == null || !customerId.equals(vehicle.getCustomer().getId())) {
            throw new InvalidAppointmentException("Vehicle with ID " + vehicleId + " does not belong to customer with ID " + customerId);
        }
    }

    @Override
    public int calculateTotalDuration(List<ServiceType> services) {
        if (services == null) {
            return 0;
        }
        return services.stream()
                .mapToInt(ServiceType::getEstimatedDuration)
                .sum();
    }

    @Override
    public List<Technician> findQualifiedTechnicians(UUID dealershipId, List<ServiceType> services) {
        if (dealershipId == null) {
            return Collections.emptyList();
        }

        List<Technician> allTechnicians = technicianRepository.findAllWithSkillsByDealershipId(dealershipId);

        if (services == null || services.isEmpty()) {
            return allTechnicians;
        }

        Set<UUID> requiredServiceTypeIds = services.stream()
                .map(ServiceType::getId)
                .collect(Collectors.toSet());

        return allTechnicians.stream()
                .filter(tech -> {
                    Set<UUID> techSkills = tech.getSkills().stream()
                            .map(skill -> skill.getServiceType().getId())
                            .collect(Collectors.toSet());
                    return techSkills.containsAll(requiredServiceTypeIds);
                })
                .toList();
    }

    @Override
    public List<Technician> findAvailableTechnicians(
            List<Technician> technicians,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        if (technicians == null || technicians.isEmpty()) {
            return Collections.emptyList();
        }

        List<Appointment> overlappingAppointments = appointmentRepository.findOverlappingActiveAppointments(startTime, endTime);

        Set<UUID> occupiedTechnicianIds = overlappingAppointments.stream()
                .map(appt -> appt.getTechnician().getId())
                .collect(Collectors.toSet());

        return technicians.stream()
                .filter(tech -> !occupiedTechnicianIds.contains(tech.getId()))
                .toList();
    }

    @Override
    public List<ServiceBay> findAvailableServiceBays(
            UUID dealershipId,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        if (dealershipId == null) {
            return Collections.emptyList();
        }

        List<ServiceBay> dealershipBays = serviceBayRepository.findByDealershipIdOrderByIdAsc(dealershipId);
        if (dealershipBays.isEmpty()) {
            return Collections.emptyList();
        }

        List<Appointment> overlappingAppointments = appointmentRepository.findOverlappingActiveAppointments(startTime, endTime);

        Set<UUID> occupiedBayIds = overlappingAppointments.stream()
                .map(appt -> appt.getServiceBay().getId())
                .collect(Collectors.toSet());

        return dealershipBays.stream()
                .filter(bay -> !occupiedBayIds.contains(bay.getId()))
                .toList();
    }
}
