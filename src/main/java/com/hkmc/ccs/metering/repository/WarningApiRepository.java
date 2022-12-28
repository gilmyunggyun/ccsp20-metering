package com.hkmc.ccs.metering.repository;

import com.hkmc.ccs.metering.models.entity.WarningApi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WarningApiRepository extends JpaRepository<WarningApi, UUID> {

    long countByRequestUrl(String requestUrl);

}
