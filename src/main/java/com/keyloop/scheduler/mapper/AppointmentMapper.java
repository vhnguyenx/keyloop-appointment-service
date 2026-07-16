package com.keyloop.scheduler.mapper;

import com.keyloop.scheduler.dto.response.AppointmentResponse;
import com.keyloop.scheduler.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    AppointmentMapper INSTANCE = Mappers.getMapper(AppointmentMapper.class);

    @Mapping(target = "appointmentId", source = "id")
    @Mapping(target = "technicianId", source = "technician.id")
    @Mapping(target = "serviceBayId", source = "serviceBay.id")
    AppointmentResponse toResponse(Appointment appointment);
}
