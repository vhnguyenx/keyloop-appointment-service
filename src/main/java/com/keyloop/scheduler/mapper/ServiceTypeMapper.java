package com.keyloop.scheduler.mapper;

import com.keyloop.scheduler.dto.response.ServiceTypeResponse;
import com.keyloop.scheduler.entity.ServiceType;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ServiceTypeMapper {

    ServiceTypeMapper INSTANCE = Mappers.getMapper(ServiceTypeMapper.class);

    ServiceTypeResponse toResponse(ServiceType serviceType);
}
