package com.keyloop.scheduler.controller;

import com.keyloop.scheduler.dto.response.ServiceTypeResponse;
import com.keyloop.scheduler.service.ServiceTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/service-types")
@RequiredArgsConstructor
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ServiceTypeResponse> getAllServiceTypes() {
        return serviceTypeService.getAllServiceTypes();
    }
}
