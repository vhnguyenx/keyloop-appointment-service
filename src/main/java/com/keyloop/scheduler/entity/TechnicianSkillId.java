package com.keyloop.scheduler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TechnicianSkillId implements Serializable {

    @Column(name = "technicianId")
    private UUID technicianId;

    @Column(name = "serviceTypeId")
    private UUID serviceTypeId;
}
