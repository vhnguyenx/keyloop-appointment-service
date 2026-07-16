package com.keyloop.scheduler.controller;

import com.keyloop.scheduler.dto.response.CustomerResponse;
import com.keyloop.scheduler.dto.response.VehicleResponse;
import com.keyloop.scheduler.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CustomerResponse> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{customerId}/vehicles")
    @ResponseStatus(HttpStatus.OK)
    public List<VehicleResponse> getCustomerVehicles(@PathVariable("customerId") UUID customerId) {
        return customerService.getCustomerVehicles(customerId);
    }
}
