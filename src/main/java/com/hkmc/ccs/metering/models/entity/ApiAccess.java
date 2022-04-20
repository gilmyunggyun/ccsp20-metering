package com.hkmc.ccs.metering.models.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "API_ACCESS")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
  @Index(
    name = "apiaccess_handphoneid_carid_requesturl",
    columnList = "handphoneid,carid,requesturl"
  )
})
public class ApiAccess {

  @Id
  @GeneratedValue
  private UUID id;

  private String handPhoneId;

  private String carId;

  private String requestUrl;

  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime accessTime;

}
