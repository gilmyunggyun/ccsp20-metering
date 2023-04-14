package com.hkmc.ccs.metering.models.entity;

import java.time.OffsetDateTime;

//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.Id;
//import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nonapi.io.github.classgraph.json.Id;

@Data
//@Entity
@Builder
//@IdClass(BlockedTempId.class)
@NoArgsConstructor
@AllArgsConstructor
public class BlockedTemp {

  @Id
  private String handPhoneId;

  @Id
  private String carId;

  private String blockedRsonCd;

  @Id
//  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime blockedTime;

  private String requestUrl;

}
