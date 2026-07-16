package com.keyloop.scheduler.repository;

import com.keyloop.scheduler.entity.ServiceBay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceBayRepository extends JpaRepository<ServiceBay, UUID> {

    List<ServiceBay> findByDealershipIdOrderByIdAsc(UUID dealershipId);
}
