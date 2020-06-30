package com.hyundaiautoever.ccs.metering;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
@Builder
public class BlockedId implements Serializable {

    private String handPhoneId;
    private String carId;

}
