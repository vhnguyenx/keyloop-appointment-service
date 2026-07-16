package com.keyloop.scheduler.repository;

import com.keyloop.scheduler.entity.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, UUID> {

    @Query("SELECT DISTINCT t FROM Technician t LEFT JOIN FETCH t.skills s LEFT JOIN FETCH s.serviceType")
    List<Technician> findAllWithSkills();

    @Query("SELECT DISTINCT t FROM Technician t LEFT JOIN FETCH t.skills s LEFT JOIN FETCH s.serviceType WHERE t.dealership.id = :dealershipId ORDER BY t.id ASC")
    List<Technician> findAllWithSkillsByDealershipId(@Param("dealershipId") UUID dealershipId);
}
