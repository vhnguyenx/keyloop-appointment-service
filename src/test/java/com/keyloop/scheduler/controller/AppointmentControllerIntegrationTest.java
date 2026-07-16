package com.keyloop.scheduler.controller;

import tools.jackson.databind.ObjectMapper;
import com.keyloop.scheduler.dto.request.CreateAppointmentRequest;
import com.keyloop.scheduler.dto.response.AppointmentResponse;
import com.keyloop.scheduler.enums.AppointmentStatus;
import com.keyloop.scheduler.exception.ResourceNotFoundException;
import com.keyloop.scheduler.exception.ResourceUnavailableException;
import com.keyloop.scheduler.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppointmentService appointmentService;

    @Test
    void shouldReturnCreatedWhenBookingIsSuccessful() throws Exception {
        // Arrange
        UUID apptId = UUID.randomUUID();
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

        AppointmentResponse response = AppointmentResponse.builder()
                .appointmentId(apptId)
                .status(AppointmentStatus.SCHEDULED)
                .technicianId(UUID.randomUUID())
                .serviceBayId(UUID.randomUUID())
                .startTime(startTime)
                .endTime(startTime.plusMinutes(30))
                .build();

        when(appointmentService.createAppointment(any(CreateAppointmentRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appointmentId").value(apptId.toString()))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
        // Arrange - missing dealershipId and empty serviceTypeIds
        CreateAppointmentRequest request = CreateAppointmentRequest.builder()
                .customerId(UUID.randomUUID())
                .vehicleId(UUID.randomUUID())
                .requestedStartTime(LocalDateTime.now().plusDays(1))
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflictWhenResourceIsUnavailable() throws Exception {
        // Arrange
        CreateAppointmentRequest request = CreateAppointmentRequest.builder()
                .customerId(UUID.randomUUID())
                .vehicleId(UUID.randomUUID())
                .dealershipId(UUID.randomUUID())
                .serviceTypeIds(List.of(UUID.randomUUID()))
                .requestedStartTime(LocalDateTime.now().plusDays(1))
                .build();

        when(appointmentService.createAppointment(any(CreateAppointmentRequest.class)))
                .thenThrow(new ResourceUnavailableException("No technician available"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnNotFoundWhenResourceDoesNotExist() throws Exception {
        // Arrange
        CreateAppointmentRequest request = CreateAppointmentRequest.builder()
                .customerId(UUID.randomUUID())
                .vehicleId(UUID.randomUUID())
                .dealershipId(UUID.randomUUID())
                .serviceTypeIds(List.of(UUID.randomUUID()))
                .requestedStartTime(LocalDateTime.now().plusDays(1))
                .build();

        when(appointmentService.createAppointment(any(CreateAppointmentRequest.class)))
                .thenThrow(new ResourceNotFoundException("Customer not found"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
