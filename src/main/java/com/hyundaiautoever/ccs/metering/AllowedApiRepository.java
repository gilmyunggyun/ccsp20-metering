package com.hyundaiautoever.ccs.metering;

import com.hyundaiautoever.ccs.metering.models.entity.AllowedApi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AllowedApiRepository extends JpaRepository<AllowedApi, UUID> {
    long countByRequestUrl(String requestUrl);
}
