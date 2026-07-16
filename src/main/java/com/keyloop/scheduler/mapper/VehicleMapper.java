package com.keyloop.scheduler.mapper;

import com.keyloop.scheduler.dto.response.VehicleResponse;
import com.keyloop.scheduler.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    VehicleMapper INSTANCE = Mappers.getMapper(VehicleMapper.class);

    VehicleResponse toResponse(Vehicle vehicle);
}
