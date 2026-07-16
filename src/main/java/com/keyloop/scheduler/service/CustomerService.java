package com.keyloop.scheduler.service;

import com.keyloop.scheduler.dto.response.CustomerResponse;
import com.keyloop.scheduler.dto.response.VehicleResponse;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    List<CustomerResponse> getAllCustomers();

    List<VehicleResponse> getCustomerVehicles(UUID customerId);
}
