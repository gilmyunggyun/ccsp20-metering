package com.hyundaiautoever.ccs.metering.VO;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import lombok.*;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeteringCheckRequest {

    @NotBlank
    @JsonAlias({"ServiceNo","serviceNo"})
    private String serviceNo;

    @NotBlank
    @JsonAlias({"CCID","hpId"})
    private String hpId;

    @NotBlank
    @JsonAlias({"carID","carId"})
    private String carId;

    @NotBlank
    private String reqUrl;

}
