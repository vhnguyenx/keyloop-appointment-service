package com.keyloop.scheduler.service;

import com.keyloop.scheduler.TestDataBuilder;
import com.keyloop.scheduler.entity.*;
import com.keyloop.scheduler.enums.AppointmentStatus;
import com.keyloop.scheduler.exception.InvalidAppointmentException;
import com.keyloop.scheduler.exception.ResourceNotFoundException;
import com.keyloop.scheduler.repository.*;
import com.keyloop.scheduler.service.impl.AvailabilityServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private TechnicianRepository technicianRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ServiceBayRepository serviceBayRepository;

    @InjectMocks
    private AvailabilityServiceImpl availabilityService;

    @Test
    void shouldValidateCustomerSuccessfullyWhenCustomerExists() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        when(customerRepository.existsById(customerId)).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> availabilityService.validateCustomer(customerId));
        verify(customerRepository, times(1)).existsById(customerId);
    }

    @Test
    void shouldThrowExceptionWhenCustomerDoesNotExist() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        when(customerRepository.existsById(customerId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> availabilityService.validateCustomer(customerId));
    }

    @Test
    void shouldValidateVehicleSuccessfullyWhenVehicleBelongsToCustomer() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        Customer customer = TestDataBuilder.buildCustomer(customerId, "John Doe", "john@example.com", "555-1111");
        Vehicle vehicle = TestDataBuilder.buildVehicle(vehicleId, customer, "12345678901234567", "Honda", "Civic", 2020);

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        // Act & Assert
        assertDoesNotThrow(() -> availabilityService.validateVehicle(customerId, vehicleId));
    }

    @Test
    void shouldThrowExceptionWhenVehicleDoesNotBelongToCustomer() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        UUID otherCustomerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        Customer otherCustomer = TestDataBuilder.buildCustomer(otherCustomerId, "Jane Doe", "jane@example.com", "555-2222");
        Vehicle vehicle = TestDataBuilder.buildVehicle(vehicleId, otherCustomer, "12345678901234567", "Honda", "Civic", 2020);

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        // Act & Assert
        assertThrows(InvalidAppointmentException.class, () -> availabilityService.validateVehicle(customerId, vehicleId));
    }

    @Test
    void shouldCalculateTotalDurationWithOneService() {
        // Arrange
        ServiceType service = TestDataBuilder.buildServiceType(UUID.randomUUID(), "Oil Change", "Desc", 30);

        // Act
        int duration = availabilityService.calculateTotalDuration(List.of(service));

        // Assert
        assertEquals(30, duration);
    }

    @Test
    void shouldCalculateTotalDurationWithMultipleServices() {
        // Arrange
        ServiceType s1 = TestDataBuilder.buildServiceType(UUID.randomUUID(), "Oil Change", "Desc", 30);
        ServiceType s2 = TestDataBuilder.buildServiceType(UUID.randomUUID(), "Tire Rotation", "Desc", 20);

        // Act
        int duration = availabilityService.calculateTotalDuration(List.of(s1, s2));

        // Assert
        assertEquals(50, duration);
    }

    @Test
    void shouldFindQualifiedTechnicians() {
        // Arrange
        UUID techId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        Dealership dealership = TestDataBuilder.buildDealership(UUID.randomUUID(), "Dealer", "Address");
        Technician technician = TestDataBuilder.buildTechnician(techId, dealership, "Tech 1");
        ServiceType service = TestDataBuilder.buildServiceType(serviceId, "Oil Change", "Desc", 30);
        TestDataBuilder.buildTechnicianSkill(technician, service);

        when(technicianRepository.findAllWithSkillsByDealershipId(dealership.getId())).thenReturn(List.of(technician));

        // Act
        List<Technician> qualified = availabilityService.findQualifiedTechnicians(dealership.getId(), List.of(service));

        // Assert
        assertEquals(1, qualified.size());
        assertEquals("Tech 1", qualified.get(0).getFullName());
    }

    @Test
    void shouldReturnEmptyListWhenNoQualifiedTechnician() {
        // Arrange
        UUID techId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UUID otherServiceId = UUID.randomUUID();
        Dealership dealership = TestDataBuilder.buildDealership(UUID.randomUUID(), "Dealer", "Address");
        Technician technician = TestDataBuilder.buildTechnician(techId, dealership, "Tech 1");
        ServiceType service = TestDataBuilder.buildServiceType(serviceId, "Oil Change", "Desc", 30);
        ServiceType requiredService = TestDataBuilder.buildServiceType(otherServiceId, "Brake Inspection", "Desc", 45);
        TestDataBuilder.buildTechnicianSkill(technician, service);

        when(technicianRepository.findAllWithSkillsByDealershipId(dealership.getId())).thenReturn(List.of(technician));

        // Act
        List<Technician> qualified = availabilityService.findQualifiedTechnicians(dealership.getId(), List.of(requiredService));

        // Assert
        assertTrue(qualified.isEmpty());
    }

    @Test
    void shouldFindAvailableTechnicians() {
        // Arrange
        UUID techId1 = UUID.randomUUID();
        UUID techId2 = UUID.randomUUID();
        Dealership dealership = TestDataBuilder.buildDealership(UUID.randomUUID(), "Dealer", "Address");
        Technician tech1 = TestDataBuilder.buildTechnician(techId1, dealership, "Tech 1");
        Technician tech2 = TestDataBuilder.buildTechnician(techId2, dealership, "Tech 2");

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMinutes(30);

        when(appointmentRepository.findOverlappingActiveAppointments(start, end)).thenReturn(Collections.emptyList());

        // Act
        List<Technician> available = availabilityService.findAvailableTechnicians(List.of(tech1, tech2), start, end);

        // Assert
        assertEquals(2, available.size());
    }

    @Test
    void shouldExcludeTechnicianWhenTechnicianHasOverlappingAppointment() {
        // Arrange
        UUID techId1 = UUID.randomUUID();
        UUID techId2 = UUID.randomUUID();
        Dealership dealership = TestDataBuilder.buildDealership(UUID.randomUUID(), "Dealer", "Address");
        Technician tech1 = TestDataBuilder.buildTechnician(techId1, dealership, "Tech 1");
        Technician tech2 = TestDataBuilder.buildTechnician(techId2, dealership, "Tech 2");

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMinutes(30);

        Customer customer = TestDataBuilder.buildCustomer(UUID.randomUUID(), "John", "john@example.com", "555-1111");
        Vehicle vehicle = TestDataBuilder.buildVehicle(UUID.randomUUID(), customer, "12345678901234567", "Honda", "Civic", 2020);
        ServiceBay bay = TestDataBuilder.buildServiceBay(UUID.randomUUID(), dealership, "Bay 1");

        Appointment overlappingAppt = TestDataBuilder.buildAppointment(UUID.randomUUID(), customer, vehicle, dealership,
                tech1, bay, start.minusMinutes(10), end.minusMinutes(10), 30, AppointmentStatus.SCHEDULED);

        when(appointmentRepository.findOverlappingActiveAppointments(start, end)).thenReturn(List.of(overlappingAppt));

        // Act
        List<Technician> available = availabilityService.findAvailableTechnicians(List.of(tech1, tech2), start, end);

        // Assert
        assertEquals(1, available.size());
        assertEquals("Tech 2", available.get(0).getFullName());
    }

    @Test
    void shouldFindAvailableServiceBays() {
        // Arrange
        UUID dealerId = UUID.randomUUID();
        Dealership dealership = TestDataBuilder.buildDealership(dealerId, "Dealer", "Address");
        ServiceBay bay1 = TestDataBuilder.buildServiceBay(UUID.randomUUID(), dealership, "Bay 1");
        ServiceBay bay2 = TestDataBuilder.buildServiceBay(UUID.randomUUID(), dealership, "Bay 2");

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMinutes(30);

        when(serviceBayRepository.findByDealershipIdOrderByIdAsc(dealerId)).thenReturn(List.of(bay1, bay2));
        when(appointmentRepository.findOverlappingActiveAppointments(start, end)).thenReturn(Collections.emptyList());

        // Act
        List<ServiceBay> available = availabilityService.findAvailableServiceBays(dealerId, start, end);

        // Assert
        assertEquals(2, available.size());
    }

    @Test
    void shouldExcludeServiceBayWhenServiceBayIsOccupied() {
        // Arrange
        UUID dealerId = UUID.randomUUID();
        Dealership dealership = TestDataBuilder.buildDealership(dealerId, "Dealer", "Address");
        ServiceBay bay1 = TestDataBuilder.buildServiceBay(UUID.randomUUID(), dealership, "Bay 1");
        ServiceBay bay2 = TestDataBuilder.buildServiceBay(UUID.randomUUID(), dealership, "Bay 2");

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMinutes(30);

        Customer customer = TestDataBuilder.buildCustomer(UUID.randomUUID(), "John", "john@example.com", "555-1111");
        Vehicle vehicle = TestDataBuilder.buildVehicle(UUID.randomUUID(), customer, "12345678901234567", "Honda", "Civic", 2020);
        Technician tech = TestDataBuilder.buildTechnician(UUID.randomUUID(), dealership, "Tech 1");

        Appointment overlappingAppt = TestDataBuilder.buildAppointment(UUID.randomUUID(), customer, vehicle, dealership,
                tech, bay1, start.minusMinutes(10), end.minusMinutes(10), 30, AppointmentStatus.SCHEDULED);

        when(serviceBayRepository.findByDealershipIdOrderByIdAsc(dealerId)).thenReturn(List.of(bay1, bay2));
        when(appointmentRepository.findOverlappingActiveAppointments(start, end)).thenReturn(List.of(overlappingAppt));

        // Act
        List<ServiceBay> available = availabilityService.findAvailableServiceBays(dealerId, start, end);

        // Assert
        assertEquals(1, available.size());
        assertEquals("Bay 2", available.get(0).getName());
    }
}
