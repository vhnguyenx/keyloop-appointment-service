package com.keyloop.scheduler.service.impl;

import com.keyloop.scheduler.dto.request.CreateAppointmentRequest;
import com.keyloop.scheduler.dto.response.AppointmentResponse;
import com.keyloop.scheduler.entity.Appointment;
import com.keyloop.scheduler.entity.AppointmentItem;
import com.keyloop.scheduler.entity.Customer;
import com.keyloop.scheduler.entity.Dealership;
import com.keyloop.scheduler.entity.ServiceBay;
import com.keyloop.scheduler.entity.ServiceType;
import com.keyloop.scheduler.entity.Technician;
import com.keyloop.scheduler.entity.Vehicle;
import com.keyloop.scheduler.enums.AppointmentStatus;
import com.keyloop.scheduler.exception.ResourceNotFoundException;
import com.keyloop.scheduler.mapper.AppointmentMapper;
import com.keyloop.scheduler.repository.AppointmentRepository;
import com.keyloop.scheduler.repository.CustomerRepository;
import com.keyloop.scheduler.repository.DealershipRepository;
import com.keyloop.scheduler.repository.ServiceTypeRepository;
import com.keyloop.scheduler.repository.AppointmentItemRepository;
import com.keyloop.scheduler.repository.VehicleRepository;
import com.keyloop.scheduler.service.AppointmentService;
import com.keyloop.scheduler.service.assignment.AssignmentService;
import com.keyloop.scheduler.service.availability.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final DealershipRepository dealershipRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentItemRepository appointmentItemRepository;
    private final AvailabilityService availabilityService;
    private final AssignmentService assignmentService;
    private final AppointmentMapper appointmentMapper;

    @Override
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        // 1. Validate customer exists
        availabilityService.validateCustomer(request.getCustomerId());

        // 2. Validate vehicle belongs to customer
        availabilityService.validateVehicle(request.getCustomerId(), request.getVehicleId());

        // 3. Load dealership
        Dealership dealership = dealershipRepository.findById(request.getDealershipId())
                .orElseThrow(() -> new ResourceNotFoundException("Dealership with ID " + request.getDealershipId() + " not found"));

        // 4. Load all requested ServiceTypes
        List<ServiceType> serviceTypes = serviceTypeRepository.findAllById(request.getServiceTypeIds());
        Map<UUID, ServiceType> serviceTypeMap = serviceTypes.stream()
                .collect(Collectors.toMap(ServiceType::getId, Function.identity()));

        for (UUID serviceTypeId : request.getServiceTypeIds()) {
            if (!serviceTypeMap.containsKey(serviceTypeId)) {
                throw new ResourceNotFoundException("ServiceType with ID " + serviceTypeId + " not found");
            }
        }

        // 5. Calculate total appointment duration
        int totalDuration = availabilityService.calculateTotalDuration(serviceTypes);

        // 6. Calculate appointment end time
        LocalDateTime endTime = request.getRequestedStartTime().plusMinutes(totalDuration);

        // 7. Find qualified technicians
        List<Technician> qualifiedTechs = availabilityService.findQualifiedTechnicians(request.getDealershipId(), serviceTypes);

        // 8. Find available technicians
        List<Technician> availableTechs = availabilityService.findAvailableTechnicians(
                qualifiedTechs, request.getRequestedStartTime(), endTime);

        // 9. Assign technician
        Technician assignedTech = assignmentService.assignTechnician(availableTechs);

        // 10. Find available service bays
        List<ServiceBay> availableBays = availabilityService.findAvailableServiceBays(
                request.getDealershipId(), request.getRequestedStartTime(), endTime);

        // 11. Assign service bay
        ServiceBay assignedBay = assignmentService.assignServiceBay(availableBays);

        // Fetch customer and vehicle entities
        Customer customer = customerRepository.findById(request.getCustomerId()).get();
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId()).get();

        // 12. Create Appointment entity
        Appointment appointment = Appointment.builder()
                .customer(customer)
                .vehicle(vehicle)
                .dealership(dealership)
                .technician(assignedTech)
                .serviceBay(assignedBay)
                .startTime(request.getRequestedStartTime())
                .endTime(endTime)
                .totalEstimatedDuration(totalDuration)
                .status(AppointmentStatus.SCHEDULED)
                .build();

        // 13. Create AppointmentItems preserving request order sequence
        List<AppointmentItem> items = new ArrayList<>();
        for (int i = 0; i < request.getServiceTypeIds().size(); i++) {
            UUID serviceTypeId = request.getServiceTypeIds().get(i);
            ServiceType serviceType = serviceTypeMap.get(serviceTypeId);
            AppointmentItem item = AppointmentItem.builder()
                    .appointment(appointment)
                    .serviceType(serviceType)
                    .estimatedDuration(serviceType.getEstimatedDuration())
                    .sequence(i + 1)
                    .build();
            items.add(item);
        }
        appointment.setAppointmentItems(items);

        // 14. Save Appointment and AppointmentItems
        Appointment savedAppointment = appointmentRepository.save(appointment);
        appointmentItemRepository.saveAll(items);

        // 15. Return AppointmentResponse
        return appointmentMapper.toResponse(savedAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment with ID " + appointmentId + " not found"));
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointments() {
        return appointmentRepository.findAllByOrderByStartTimeAsc().stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }
}
