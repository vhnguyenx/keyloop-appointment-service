package com.keyloop.scheduler.mapper;

import com.keyloop.scheduler.dto.response.CustomerResponse;
import com.keyloop.scheduler.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    CustomerResponse toResponse(Customer customer);
}
