package com.hkmc.ccs.metering.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.time.OffsetDateTime;

@Data
@Entity
@IdClass(BlockedId.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Unblocked {

    @Id
    private String handPhoneId;

    @Id
    private String carId;

    private String blockedRsonCd;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime blockedTime;

    private String requestId;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime requestTime;

}
