package com.hyundaiautoever.ccs.metering;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Data
@Entity
@IdClass(ApiAccessId.class)
@Builder
public class ApiAccess {
    @Id
    private String handPhoneId;
    @Id
    private String carId;
    @Id
    private String requestUrl;



}
