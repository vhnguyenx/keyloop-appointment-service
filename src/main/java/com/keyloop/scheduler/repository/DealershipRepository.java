package com.keyloop.scheduler.repository;

import com.keyloop.scheduler.entity.Dealership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DealershipRepository extends JpaRepository<Dealership, UUID> {
}
