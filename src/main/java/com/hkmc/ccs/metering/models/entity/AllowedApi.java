package com.hkmc.ccs.metering.models.entity;

import java.util.UUID;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.Id;
//import jakarta.persistence.Index;
//import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nonapi.io.github.classgraph.json.Id;

//@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
//@Table(indexes = {
//  @Index(
//    name = "allowedapi_requesturl",
//    columnList = "requesturl"
//  )
//})
public class AllowedApi {

//  @Id
//  @GeneratedValue
  UUID id;

  String requestUrl;

}
