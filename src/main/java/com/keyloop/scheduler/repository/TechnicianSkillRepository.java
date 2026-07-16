package com.keyloop.scheduler.repository;

import com.keyloop.scheduler.entity.TechnicianSkill;
import com.keyloop.scheduler.entity.TechnicianSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TechnicianSkillRepository extends JpaRepository<TechnicianSkill, TechnicianSkillId> {
}
