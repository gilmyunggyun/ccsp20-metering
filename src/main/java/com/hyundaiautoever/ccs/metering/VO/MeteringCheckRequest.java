package com.hyundaiautoever.ccs.metering.VO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeteringCheckRequest {

    @NotBlank
    private String serviceNo;

    @NotBlank
    private String hpId;

    @NotBlank
    private String carId;

    @NotBlank
    private String reqUrl;
}
