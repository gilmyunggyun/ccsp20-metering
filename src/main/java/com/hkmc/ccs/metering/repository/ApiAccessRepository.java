package com.hkmc.ccs.metering.repository;

import java.time.OffsetDateTime;
import java.util.UUID;

//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;

import com.hkmc.ccs.metering.models.entity.ApiAccess;
import feign.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiAccessRepository {

  long countByHandPhoneIdAndCarIdAndRequestUrlAndAccessTimeAfter(
    String handPhoneId,
    String carId,
    String requestUrl,
    OffsetDateTime accessTime
  );

//  @Query(nativeQuery = true, value = "select count(1) from api_Access \n" +
//                                       "where hand_Phone_Id = :handPhoneId\n" +
//                                       "and car_Id = :carId\n" +
//                                       "and request_Url = :requestUrl \n" +
//                                       "and access_time between to_timestamp(CURRENT_DATE||' 00:00:00','yyyy-mm-dd " +
//                                       "hh24:mi:ss') and to_timestamp(CURRENT_DATE||' 23:59:59','yyyy-mm-dd " +
//                                       "hh24:mi:ss') "
//  )
  long dailyAccessCount(
    @Param("handPhoneId") String handPhoneId,
    @Param("carId") String carId,
    @Param("requestUrl") String requestUrl
  );

//  @Query(nativeQuery = true, value = "select count(1) from api_Access \n" +
//                                       "where hand_Phone_Id = :handPhoneId\n" +
//                                       "and car_Id = :carId\n" +
//                                       "and request_Url = :requestUrl \n" +
//                                       "and access_time between :accessTime and CURRENT_TIMESTAMP "
//  )
  long lastTimeAccessCount(
    @Param("handPhoneId") String handPhoneId,
    @Param("carId") String carId,
    @Param("requestUrl") String requestUrl,
    @Param("accessTime") OffsetDateTime accessTime
  );

  void save(ApiAccess build);
}
