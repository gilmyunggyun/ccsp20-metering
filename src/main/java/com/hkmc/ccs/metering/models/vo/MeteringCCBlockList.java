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
public class MeteringCCBlockList {

    private String ccid;

    private String rsonCd;

    private String blockedDate;

}