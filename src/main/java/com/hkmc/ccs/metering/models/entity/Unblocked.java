package com.hkmc.ccs.metering.models.entity;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@IdClass(BlockedTempId.class)
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
