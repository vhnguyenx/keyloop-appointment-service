package com.keyloop.scheduler.service;

import com.keyloop.scheduler.dto.response.DealershipResponse;

import java.util.List;

public interface DealershipService {

    List<DealershipResponse> getAllDealerships();
}
