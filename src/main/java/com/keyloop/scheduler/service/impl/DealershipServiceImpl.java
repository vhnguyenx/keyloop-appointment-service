package com.keyloop.scheduler.service.impl;

import com.keyloop.scheduler.dto.response.DealershipResponse;
import com.keyloop.scheduler.mapper.DealershipMapper;
import com.keyloop.scheduler.repository.DealershipRepository;
import com.keyloop.scheduler.service.DealershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DealershipServiceImpl implements DealershipService {

    private final DealershipRepository dealershipRepository;
    private final DealershipMapper dealershipMapper;

    @Override
    public List<DealershipResponse> getAllDealerships() {
        return dealershipRepository.findAll().stream()
                .map(dealershipMapper::toResponse)
                .toList();
    }
}
