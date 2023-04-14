package com.hkmc.ccs.metering.repository;

import java.util.UUID;

//import org.springframework.data.jpa.repository.JpaRepository;

import com.hkmc.ccs.metering.models.entity.AllowedApi;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Repository;

@Repository
public interface AllowedApiRepository {

  long countByRequestUrl(String requestUrl);

}
