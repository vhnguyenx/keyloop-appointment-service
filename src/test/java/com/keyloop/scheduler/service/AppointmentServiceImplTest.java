package com.keyloop.scheduler.service;

import com.keyloop.scheduler.TestDataBuilder;
import com.keyloop.scheduler.dto.request.CreateAppointmentRequest;
import com.keyloop.scheduler.dto.response.AppointmentResponse;
import com.keyloop.scheduler.entity.*;
import com.keyloop.scheduler.enums.AppointmentStatus;
import com.keyloop.scheduler.exception.InvalidAppointmentException;
import com.keyloop.scheduler.exception.ResourceNotFoundException;
import com.keyloop.scheduler.exception.ResourceUnavailableException;
import com.keyloop.scheduler.mapper.AppointmentMapper;
import com.keyloop.scheduler.repository.*;
import com.keyloop.scheduler.service.assignment.AssignmentService;
import com.keyloop.scheduler.service.availability.AvailabilityService;
import com.keyloop.scheduler.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DealershipRepository dealershipRepository;

    @Mock
    private ServiceTypeRepository serviceTypeRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentItemRepository appointmentItemRepository;

    @Mock
    private AvailabilityService availabilityService;

    @Mock
    private AssignmentService assignmentService;

    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @Test
    void shouldCreateAppointmentSuccessfully() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID dealerId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);

        CreateAppointmentRequest request = CreateAppointmentRequest.builder()
                .customerId(customerId)
                .vehicleId(vehicleId)
                .dealershipId(dealerId)
                .serviceTypeIds(List.of(serviceId))
                .requestedStartTime(startTime)
                .build();

        Customer customer = TestDataBuilder.buildCustomer(customerId, "Alice Green", "alice@example.com", "555-1111");
        Vehicle vehicle = TestDataBuilder.buildVehicle(vehicleId, customer, "12345678901234567", "Honda", "Civic", 2020);
        Dealership dealership = TestDataBuilder.buildDealership(dealerId, "Northside", "123 Street");
        ServiceType serviceType = TestDataBuilder.buildServiceType(serviceId, "Oil Change", "Desc", 30);
        Technician technician = TestDataBuilder.buildTechnician(UUID.randomUUID(), dealership, "Tech 1");
        ServiceBay serviceBay = TestDataBuilder.buildServiceBay(UUID.randomUUID(), dealership, "Bay 1");

        // Stubs for validation and loads
        doNothing().when(availabilityService).validateCustomer(customerId);
        doNothing().when(availabilityService).validateVehicle(customerId, vehicleId);
        when(dealershipRepository.findById(dealerId)).thenReturn(Optional.of(dealership));
        when(serviceTypeRepository.findAllById(List.of(serviceId))).thenReturn(List.of(serviceType));

        // Stubs for business logic
        when(availabilityService.calculateTotalDuration(any())).thenReturn(30);
        when(availabilityService.findQualifiedTechnicians(any(), any())).thenReturn(List.of(technician));
        when(availabilityService.findAvailableTechnicians(any(), any(), any())).thenReturn(List.of(technician));
        when(assignmentService.assignTechnician(any())).thenReturn(technician);

        when(availabilityService.findAvailableServiceBays(any(), any(), any())).thenReturn(List.of(serviceBay));
        when(assignmentService.assignServiceBay(any())).thenReturn(serviceBay);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        // Stub save and map
        Appointment mockAppt = TestDataBuilder.buildAppointment(UUID.randomUUID(), customer, vehicle, dealership,
                technician, serviceBay, startTime, startTime.plusMinutes(30), 30, AppointmentStatus.SCHEDULED);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockAppt);

        AppointmentResponse expectedResponse = AppointmentResponse.builder()
                .appointmentId(mockAppt.getId())
                .status(AppointmentStatus.SCHEDULED)
                .technicianId(technician.getId())
                .serviceBayId(serviceBay.getId())
                .startTime(startTime)
                .endTime(startTime.plusMinutes(30))
                .build();
        when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(expectedResponse);

        // Act
        AppointmentResponse response = appointmentService.createAppointment(request);

        // Assert
        assertNotNull(response);
        assertEquals(mockAppt.getId(), response.getAppointmentId());

        // Verifications using ArgumentCaptor for entity save calls
        ArgumentCaptor<Appointment> apptCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository, times(1)).save(apptCaptor.capture());
        verify(appointmentItemRepository, times(1)).saveAll(any());

        Appointment capturedAppt = apptCaptor.getValue();
        assertEquals(customerId, capturedAppt.getCustomer().getId());
        assertEquals(vehicleId, capturedAppt.getVehicle().getId());
        assertEquals(dealerId, capturedAppt.getDealership().getId());
        assertEquals(technician.getId(), capturedAppt.getTechnician().getId());
        assertEquals(serviceBay.getId(), capturedAppt.getServiceBay().getId());
        assertEquals(1, capturedAppt.getAppointmentItems().size());
        assertEquals(1, capturedAppt.getAppointmentItems().get(0).getSequence());
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        CreateAppointmentRequest request = CreateAppointmentRequest.builder()
                .customerId(customerId)
                .build();

        doThrow(new ResourceNotFoundException("Customer not found")).when(availabilityService).validateCustomer(customerId);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> appointmentService.createAppointment(request));
    }

    @Test
    void shouldThrowExceptionWhenVehicleNotFound() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        CreateAppointmentRequest request = CreateAppointmentRequest.builder()
                .customerId(customerId)
                .vehicleId(vehicleId)
                .build();

        doNothing().when(availabilityService).validateCustomer(customerId);
        doThrow(new ResourceNotFoundException("Vehicle not found")).when(availabilityService).validateVehicle(customerId, vehicleId);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> appointmentService.createAppointment(request));
    }

    @Test
    void shouldThrowExceptionWhenInvalidVehicleOwnership() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        CreateAppointmentRequest request = CreateAppointmentRequest.builder()
                .customerId(customerId)
                .vehicleId(vehicleId)
                .build();

        doNothing().when(availabilityService).validateCustomer(customerId);
        doThrow(new InvalidAppointmentException("Vehicle ownership invalid")).when(availabilityService).validateVehicle(customerId, vehicleId);

        // Act & Assert
        assertThrows(InvalidAppointmentException.class, () -> appointmentService.createAppointment(request));
    }

    @Test
    void shouldThrowExceptionWhenNoQualifiedTechnician() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID dealerId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);

        CreateAppointmentRequest request = CreateAppointmentRequest.builder()
                .customerId(customerId)
                .vehicleId(vehicleId)
                .dealershipId(dealerId)
                .serviceTypeIds(List.of(serviceId))
                .requestedStartTime(startTime)
                .build();

        Dealership dealership = TestDataBuilder.buildDealership(dealerId, "Northside", "123 Street");
        ServiceType serviceType = TestDataBuilder.buildServiceType(serviceId, "Oil Change", "Desc", 30);

        doNothing().when(availabilityService).validateCustomer(customerId);
        doNothing().when(availabilityService).validateVehicle(customerId, vehicleId);
        when(dealershipRepository.findById(dealerId)).thenReturn(Optional.of(dealership));
        when(serviceTypeRepository.findAllById(List.of(serviceId))).thenReturn(List.of(serviceType));

        when(availabilityService.calculateTotalDuration(any())).thenReturn(30);
        when(availabilityService.findQualifiedTechnicians(any(), any())).thenReturn(Collections.emptyList());
        when(availabilityService.findAvailableTechnicians(any(), any(), any())).thenReturn(Collections.emptyList());
        when(assignmentService.assignTechnician(any())).thenThrow(new ResourceUnavailableException("No qualified technician"));

        // Act & Assert
        assertThrows(ResourceUnavailableException.class, () -> appointmentService.createAppointment(request));
    }

    @Test
    void shouldThrowExceptionWhenNoAvailableTechnician() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID dealerId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);

        CreateAppointmentRequest request = CreateAppointmentRequest.builder()
                .customerId(customerId)
                .vehicleId(vehicleId)
                .dealershipId(dealerId)
                .serviceTypeIds(List.of(serviceId))
                .requestedStartTime(startTime)
                .build();

        Dealership dealership = TestDataBuilder.buildDealership(dealerId, "Northside", "123 Street");
        ServiceType serviceType = TestDataBuilder.buildServiceType(serviceId, "Oil Change", "Desc", 30);
        Technician technician = TestDataBuilder.buildTechnician(UUID.randomUUID(), dealership, "Tech 1");

        doNothing().when(availabilityService).validateCustomer(customerId);
        doNothing().when(availabilityService).validateVehicle(customerId, vehicleId);
        when(dealershipRepository.findById(dealerId)).thenReturn(Optional.of(dealership));
        when(serviceTypeRepository.findAllById(List.of(serviceId))).thenReturn(List.of(serviceType));

        when(availabilityService.calculateTotalDuration(any())).thenReturn(30);
        when(availabilityService.findQualifiedTechnicians(any(), any())).thenReturn(List.of(technician));
        when(availabilityService.findAvailableTechnicians(any(), any(), any())).thenReturn(Collections.emptyList());
        when(assignmentService.assignTechnician(any())).thenThrow(new ResourceUnavailableException("No available technician"));

        // Act & Assert
        assertThrows(ResourceUnavailableException.class, () -> appointmentService.createAppointment(request));
    }

    @Test
    void shouldThrowExceptionWhenNoAvailableServiceBay() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID dealerId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);

        CreateAppointmentRequest request = CreateAppointmentRequest.builder()
                .customerId(customerId)
                .vehicleId(vehicleId)
                .dealershipId(dealerId)
                .serviceTypeIds(List.of(serviceId))
                .requestedStartTime(startTime)
                .build();

        Dealership dealership = TestDataBuilder.buildDealership(dealerId, "Northside", "123 Street");
        ServiceType serviceType = TestDataBuilder.buildServiceType(serviceId, "Oil Change", "Desc", 30);
        Technician technician = TestDataBuilder.buildTechnician(UUID.randomUUID(), dealership, "Tech 1");

        doNothing().when(availabilityService).validateCustomer(customerId);
        doNothing().when(availabilityService).validateVehicle(customerId, vehicleId);
        when(dealershipRepository.findById(dealerId)).thenReturn(Optional.of(dealership));
        when(serviceTypeRepository.findAllById(List.of(serviceId))).thenReturn(List.of(serviceType));

        when(availabilityService.calculateTotalDuration(any())).thenReturn(30);
        when(availabilityService.findQualifiedTechnicians(any(), any())).thenReturn(List.of(technician));
        when(availabilityService.findAvailableTechnicians(any(), any(), any())).thenReturn(List.of(technician));
        when(assignmentService.assignTechnician(any())).thenReturn(technician);

        when(availabilityService.findAvailableServiceBays(any(), any(), any())).thenReturn(Collections.emptyList());
        when(assignmentService.assignServiceBay(any())).thenThrow(new ResourceUnavailableException("No available bay"));

        // Act & Assert
        assertThrows(ResourceUnavailableException.class, () -> appointmentService.createAppointment(request));
    }
}
