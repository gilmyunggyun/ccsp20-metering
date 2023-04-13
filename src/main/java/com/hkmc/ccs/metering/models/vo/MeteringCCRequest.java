package com.hkmc.ccs.metering.models.vo;

//import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeteringCCRequest {

  @NotBlank
  @JsonAlias({"carId", "carid", "carID"})
  private String carId;

  @NotBlank
  @JsonAlias({"requestId", "RequestId", "requestID"})
  private String requestId;

}
