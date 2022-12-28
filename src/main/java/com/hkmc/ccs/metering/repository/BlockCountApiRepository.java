package com.hkmc.ccs.metering.repository;

import com.hkmc.ccs.metering.models.entity.BlockCountApi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BlockCountApiRepository extends JpaRepository<BlockCountApi, UUID> {

    long countByRequestUrl(String requestUrl);

}
