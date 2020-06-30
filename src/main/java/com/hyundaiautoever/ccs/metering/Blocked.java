package com.hyundaiautoever.ccs.metering;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Data
@Entity
@IdClass(BlockedId.class)
@Builder
public class Blocked {

    @Id
    private String handPhoneId;
    @Id
    private String carId;


}
