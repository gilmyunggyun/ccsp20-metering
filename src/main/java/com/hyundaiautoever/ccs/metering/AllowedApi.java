package com.hyundaiautoever.ccs.metering;

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
                name = "allowedapi_requesturl",
                columnList = "requesturl"
        )
})
public class AllowedApi {

    @Id
    @GeneratedValue
    UUID id;

    String requestUrl;
}
