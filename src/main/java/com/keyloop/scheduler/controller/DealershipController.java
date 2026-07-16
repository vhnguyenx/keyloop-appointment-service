package com.keyloop.scheduler.controller;

import com.keyloop.scheduler.dto.response.DealershipResponse;
import com.keyloop.scheduler.service.DealershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dealerships")
@RequiredArgsConstructor
public class DealershipController {

    private final DealershipService dealershipService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<DealershipResponse> getAllDealerships() {
        return dealershipService.getAllDealerships();
    }
}
