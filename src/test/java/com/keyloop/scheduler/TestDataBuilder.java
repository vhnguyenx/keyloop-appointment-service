package com.keyloop.scheduler;

import com.keyloop.scheduler.entity.*;
import com.keyloop.scheduler.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class TestDataBuilder {

    public static Customer buildCustomer(UUID id, String fullName, String email, String phone) {
        Customer customer = Customer.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .build();
        customer.setId(id);
        return customer;
    }

    public static Vehicle buildVehicle(UUID id, Customer customer, String vin, String make, String model, int year) {
        Vehicle vehicle = Vehicle.builder()
                .customer(customer)
                .vin(vin)
                .make(make)
                .model(model)
                .year(year)
                .build();
        vehicle.setId(id);
        return vehicle;
    }

    public static Dealership buildDealership(UUID id, String name, String address) {
        Dealership dealership = Dealership.builder()
                .name(name)
                .address(address)
                .build();
        dealership.setId(id);
        return dealership;
    }

    public static ServiceBay buildServiceBay(UUID id, Dealership dealership, String name) {
        ServiceBay serviceBay = ServiceBay.builder()
                .dealership(dealership)
                .name(name)
                .build();
        serviceBay.setId(id);
        return serviceBay;
    }

    public static ServiceType buildServiceType(UUID id, String name, String description, int estimatedDuration) {
        ServiceType serviceType = ServiceType.builder()
                .name(name)
                .description(description)
                .estimatedDuration(estimatedDuration)
                .build();
        serviceType.setId(id);
        return serviceType;
    }

    public static Technician buildTechnician(UUID id, Dealership dealership, String fullName) {
        Technician technician = Technician.builder()
                .dealership(dealership)
                .fullName(fullName)
                .skills(new HashSet<>())
                .build();
        technician.setId(id);
        return technician;
    }

    public static TechnicianSkill buildTechnicianSkill(Technician technician, ServiceType serviceType) {
        TechnicianSkillId skillId = new TechnicianSkillId(technician.getId(), serviceType.getId());
        TechnicianSkill skill = TechnicianSkill.builder()
                .id(skillId)
                .technician(technician)
                .serviceType(serviceType)
                .build();
        technician.getSkills().add(skill);
        return skill;
    }

    public static Appointment buildAppointment(UUID id, Customer customer, Vehicle vehicle, Dealership dealership,
                                               Technician technician, ServiceBay serviceBay, LocalDateTime startTime,
                                               LocalDateTime endTime, int duration, AppointmentStatus status) {
        Appointment appointment = Appointment.builder()
                .customer(customer)
                .vehicle(vehicle)
                .dealership(dealership)
                .technician(technician)
                .serviceBay(serviceBay)
                .startTime(startTime)
                .endTime(endTime)
                .totalEstimatedDuration(duration)
                .status(status)
                .appointmentItems(new ArrayList<>())
                .build();
        appointment.setId(id);
        return appointment;
    }

    public static AppointmentItem buildAppointmentItem(UUID id, Appointment appointment, ServiceType serviceType,
                                                       int estimatedDuration, int sequence) {
        AppointmentItem item = AppointmentItem.builder()
                .appointment(appointment)
                .serviceType(serviceType)
                .estimatedDuration(estimatedDuration)
                .sequence(sequence)
                .build();
        item.setId(id);
        appointment.getAppointmentItems().add(item);
        return item;
    }
}
