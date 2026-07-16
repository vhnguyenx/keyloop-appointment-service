package com.keyloop.scheduler.service.assignment;

import com.keyloop.scheduler.entity.ServiceBay;
import com.keyloop.scheduler.entity.Technician;

import java.util.List;

public interface AssignmentService {

    Technician assignTechnician(List<Technician> technicians);

    ServiceBay assignServiceBay(List<ServiceBay> bays);
}
