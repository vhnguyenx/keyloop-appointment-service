package com.keyloop.scheduler.service;

import com.keyloop.scheduler.TestDataBuilder;
import com.keyloop.scheduler.entity.Dealership;
import com.keyloop.scheduler.entity.ServiceBay;
import com.keyloop.scheduler.entity.Technician;
import com.keyloop.scheduler.exception.ResourceUnavailableException;
import com.keyloop.scheduler.service.impl.AssignmentServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentServiceImplTest {

    private final AssignmentServiceImpl assignmentService = new AssignmentServiceImpl();

    @Test
    void shouldAssignFirstAvailableTechnician() {
        // Arrange
        Dealership dealership = TestDataBuilder.buildDealership(UUID.randomUUID(), "Dealer", "Address");
        Technician tech1 = TestDataBuilder.buildTechnician(UUID.randomUUID(), dealership, "Tech 1");
        Technician tech2 = TestDataBuilder.buildTechnician(UUID.randomUUID(), dealership, "Tech 2");

        // Act
        Technician assigned = assignmentService.assignTechnician(List.of(tech1, tech2));

        // Assert
        assertNotNull(assigned);
        assertEquals("Tech 1", assigned.getFullName());
    }

    @Test
    void shouldThrowExceptionWhenNoTechnicianAvailable() {
        // Arrange & Act & Assert
        assertThrows(ResourceUnavailableException.class, () -> assignmentService.assignTechnician(Collections.emptyList()));
        assertThrows(ResourceUnavailableException.class, () -> assignmentService.assignTechnician(null));
    }

    @Test
    void shouldAssignFirstAvailableServiceBay() {
        // Arrange
        Dealership dealership = TestDataBuilder.buildDealership(UUID.randomUUID(), "Dealer", "Address");
        ServiceBay bay1 = TestDataBuilder.buildServiceBay(UUID.randomUUID(), dealership, "Bay 1");
        ServiceBay bay2 = TestDataBuilder.buildServiceBay(UUID.randomUUID(), dealership, "Bay 2");

        // Act
        ServiceBay assigned = assignmentService.assignServiceBay(List.of(bay1, bay2));

        // Assert
        assertNotNull(assigned);
        assertEquals("Bay 1", assigned.getName());
    }

    @Test
    void shouldThrowExceptionWhenNoServiceBayAvailable() {
        // Arrange & Act & Assert
        assertThrows(ResourceUnavailableException.class, () -> assignmentService.assignServiceBay(Collections.emptyList()));
        assertThrows(ResourceUnavailableException.class, () -> assignmentService.assignServiceBay(null));
    }
}
