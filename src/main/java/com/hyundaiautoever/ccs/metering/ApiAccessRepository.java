package com.hyundaiautoever.ccs.metering;

import com.hyundaiautoever.ccs.metering.models.entity.ApiAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("select count(a) from ApiAccess a \n" +
            "where a.handPhoneId = :handPhoneId\n" +
            "and a.carId = :carId\n" +
            "and a.requestUrl = :requestUrl \n" +
            "and a.accessTime >= CURRENT_DATE "
    )
    long dailyAccessCount(
            @Param("handPhoneId") String handPhoneId,
            @Param("carId") String carId,
            @Param("requestUrl") String requestUrl
    );
}
