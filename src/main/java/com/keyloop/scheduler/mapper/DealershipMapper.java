package com.keyloop.scheduler.mapper;

import com.keyloop.scheduler.dto.response.DealershipResponse;
import com.keyloop.scheduler.entity.Dealership;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DealershipMapper {

    DealershipMapper INSTANCE = Mappers.getMapper(DealershipMapper.class);

    DealershipResponse toResponse(Dealership dealership);
}
