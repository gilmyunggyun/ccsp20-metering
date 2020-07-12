package com.hyundaiautoever.ccs.metering.VO;

import com.fasterxml.jackson.annotation.JsonAlias;
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


    @JsonAlias({"serviceNo","ServiceNo"})
    private String ServiceNo;

    @JsonProperty("retCode")
    private String RetCode;

    private String resCode;


}