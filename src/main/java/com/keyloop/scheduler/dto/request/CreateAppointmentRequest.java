package com.keyloop.scheduler.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAppointmentRequest {

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;

    @NotNull(message = "Dealership ID is required")
    private UUID dealershipId;

    @NotEmpty(message = "At least one Service Type ID is required")
    private List<@NotNull UUID> serviceTypeIds;

    @NotNull(message = "Requested start time is required")
    @FutureOrPresent(message = "Requested start time must be in the present or future")
    private LocalDateTime requestedStartTime;
}
