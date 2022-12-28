package com.hkmc.ccs.metering.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {
        @Index(
                name = "warningapi_requesturl",
                columnList = "requestUrl"
        )
})
public class WarningApi {

        @Id
        @GeneratedValue
        UUID id;

        String requestUrl;

}
