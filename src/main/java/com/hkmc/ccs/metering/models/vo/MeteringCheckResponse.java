package com.hkmc.ccs.metering.models.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MeteringCheckResponse {

    @JsonProperty("ServiceNo")
    private String serviceNo;

    @JsonProperty("RetCode")
    private String retCode;

    private String resCode;
}