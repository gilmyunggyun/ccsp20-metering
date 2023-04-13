package com.hkmc.ccs.metering.models.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@IdClass(BlockedId.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Blocked {

  @Id
  private String handPhoneId;

  @Id
  private String carId;

  private String blockedRsonCd;

  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime blockedTime;

}
