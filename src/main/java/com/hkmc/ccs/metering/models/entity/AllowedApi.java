package com.hkmc.ccs.metering.models.entity;

import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
