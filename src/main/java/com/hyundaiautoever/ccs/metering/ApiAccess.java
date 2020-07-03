package com.hyundaiautoever.ccs.metering;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(
                name = "apiaccess_handphoneid_carid_requesturl",
                columnList = "handphoneid,carid,requesturl"
        )
})
public class ApiAccess {

    @Id
    @GeneratedValue
    private UUID id;

    private String handPhoneId;

    private String carId;

    private String requestUrl;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime accessTime;
}
