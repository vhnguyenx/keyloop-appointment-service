package com.keyloop.scheduler.service.impl;

import com.keyloop.scheduler.entity.ServiceBay;
import com.keyloop.scheduler.entity.Technician;
import com.keyloop.scheduler.exception.ResourceUnavailableException;
import com.keyloop.scheduler.service.assignment.AssignmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    @Override
    public Technician assignTechnician(List<Technician> technicians) {
        if (technicians == null || technicians.isEmpty()) {
            throw new ResourceUnavailableException("No available technicians found for the requested slot");
        }
        return technicians.get(0);
    }

    @Override
    public ServiceBay assignServiceBay(List<ServiceBay> bays) {
        if (bays == null || bays.isEmpty()) {
            throw new ResourceUnavailableException("No available service bays found for the requested slot");
        }
        return bays.get(0);
    }
}
