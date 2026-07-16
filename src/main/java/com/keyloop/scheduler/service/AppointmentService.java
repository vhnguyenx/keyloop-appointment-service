package com.keyloop.scheduler.service;

import com.keyloop.scheduler.dto.request.CreateAppointmentRequest;
import com.keyloop.scheduler.dto.response.AppointmentResponse;

import java.util.List;
import java.util.UUID;

public interface AppointmentService {

    AppointmentResponse createAppointment(CreateAppointmentRequest request);

    AppointmentResponse getAppointment(UUID appointmentId);

    List<AppointmentResponse> getAppointments();
}
