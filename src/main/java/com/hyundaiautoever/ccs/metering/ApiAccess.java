package com.hyundaiautoever.ccs.metering;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.time.OffsetDateTime;

@Data
@Entity
@IdClass(ApiAccessId.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiAccess {
    @Id
    private String handPhoneId;

    @Id
    private String carId;

    @Id
    private String requestUrl;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime accessTime;
}
