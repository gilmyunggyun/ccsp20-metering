package com.hkmc.ccs.metering.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockedTempId implements Serializable {

    private String handPhoneId;
    private String carId;
    private OffsetDateTime blockedTime;

}
