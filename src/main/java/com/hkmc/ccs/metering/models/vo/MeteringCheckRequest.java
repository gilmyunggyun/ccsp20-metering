package com.hkmc.ccs.metering.models.vo;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeteringCheckRequest {

  @NotBlank
  @JsonAlias({"serviceNo", "ServiceNo"})
  private String serviceNo;

  @NotBlank
  @JsonAlias({"hpId", "ccid", "ccId", "CCID"})
  private String hpId;

  @NotBlank
  @JsonAlias({"carId", "carid", "carID"})
  private String carId;

  @NotBlank
  private String reqUrl;

}
