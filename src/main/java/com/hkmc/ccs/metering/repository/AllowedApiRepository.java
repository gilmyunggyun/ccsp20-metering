package com.hkmc.ccs.metering.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hkmc.ccs.metering.models.entity.AllowedApi;

public interface AllowedApiRepository extends JpaRepository<AllowedApi, UUID> {

  long countByRequestUrl(String requestUrl);

}
