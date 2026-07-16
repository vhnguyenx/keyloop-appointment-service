package com.keyloop.scheduler.service.impl;

import com.keyloop.scheduler.dto.response.ServiceTypeResponse;
import com.keyloop.scheduler.mapper.ServiceTypeMapper;
import com.keyloop.scheduler.repository.ServiceTypeRepository;
import com.keyloop.scheduler.service.ServiceTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceTypeServiceImpl implements ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepository;
    private final ServiceTypeMapper serviceTypeMapper;

    @Override
    public List<ServiceTypeResponse> getAllServiceTypes() {
        return serviceTypeRepository.findAll().stream()
                .map(serviceTypeMapper::toResponse)
                .toList();
    }
}
