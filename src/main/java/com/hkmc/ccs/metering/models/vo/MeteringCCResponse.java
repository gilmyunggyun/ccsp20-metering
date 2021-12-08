package com.hkmc.ccs.metering.models.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeteringCCResponse {

  @JsonProperty("resultCode")
  private String resultCode;

  @JsonProperty("resultMessage")
  private String resultMessage;

  private String carId;

  private List<MeteringCCBlockList> blockList;

}