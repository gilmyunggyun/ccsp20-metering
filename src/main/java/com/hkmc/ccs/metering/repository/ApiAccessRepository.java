package com.hkmc.ccs.metering.repository;

import com.hkmc.ccs.metering.models.entity.ApiAccess;
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


    @Query(nativeQuery = true, value = "select count(1) from api_Access \n" +
            "where hand_Phone_Id = :handPhoneId\n" +
            "and car_Id = :carId\n" +
            "and request_Url = :requestUrl \n" +
            "and access_Time >= CURRENT_DATE "
    )
    long dailyAccessCount(
            @Param("handPhoneId") String handPhoneId,
            @Param("carId") String carId,
            @Param("requestUrl") String requestUrl
    );
}
