package com.keyloop.scheduler.service.impl;

import com.keyloop.scheduler.dto.response.CustomerResponse;
import com.keyloop.scheduler.dto.response.VehicleResponse;
import com.keyloop.scheduler.exception.ResourceNotFoundException;
import com.keyloop.scheduler.mapper.CustomerMapper;
import com.keyloop.scheduler.mapper.VehicleMapper;
import com.keyloop.scheduler.repository.CustomerRepository;
import com.keyloop.scheduler.repository.VehicleRepository;
import com.keyloop.scheduler.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerMapper customerMapper;
    private final VehicleMapper vehicleMapper;

    @Override
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Override
    public List<VehicleResponse> getCustomerVehicles(UUID customerId) {
        if (customerId == null || !customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer with ID " + customerId + " not found");
        }
        return vehicleRepository.findByCustomerId(customerId).stream()
                .map(vehicleMapper::toResponse)
                .toList();
    }
}
