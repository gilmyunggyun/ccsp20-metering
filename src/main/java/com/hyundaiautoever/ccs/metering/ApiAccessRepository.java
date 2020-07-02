package com.hyundaiautoever.ccs.metering;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface ApiAccessRepository extends JpaRepository<ApiAccess, UUID> {
    long countByHandPhoneIdAndCarIdAndRequestUrlAndAccessTimeAfter(
            String handPhoneId,
            String carId,
            String requestUrl,
            OffsetDateTime accessTime
    );
}
