package com.hkmc.ccs.metering.models.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sun.istack.NotNull;
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
public class MeteringCCRequest {

    @NotBlank
    @JsonAlias({"carId","carid","carID"})
    private String carId;

    @NotBlank
    @JsonAlias({"requestId","RequestId","requestID"})
    private String requestId;

}
