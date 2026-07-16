package com.keyloop.scheduler.service;

import com.keyloop.scheduler.dto.response.ServiceTypeResponse;

import java.util.List;

public interface ServiceTypeService {

    List<ServiceTypeResponse> getAllServiceTypes();
}
