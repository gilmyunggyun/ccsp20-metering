package com.hkmc.ccs.metering.repository;

import com.hkmc.ccs.metering.models.entity.AllowedApi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AllowedApiRepository extends JpaRepository<AllowedApi, UUID> {
    long countByRequestUrl(String requestUrl);
}
