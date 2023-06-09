package com.hkmc.ccs.metering.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;

import com.hkmc.ccs.metering.models.entity.*;
import com.hkmc.ccs.metering.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import com.hkmc.ccs.metering.models.vo.MeteringCheckRequest;

@Slf4j
@Service
public class MeteringService {

  private  BlockedRepository blockedRepository;

  private  BlockedTempRepository blockedTempRepository;

  private  ApiAccessRepository apiAccessRepository;

  private  AllowedApiRepository allowedApiRepository;

  private final Clock clock;

  @Value("${metering.max-ten-minute-access-count}")
  private int maxTenMinuteAccessCount = 200;

  @Value("${metering.max-day-access-count}")
  private int maxDayAccessCount = 300;

  @Value("${metering.caridnullablewhitelist}")
  private String[] remoteControlWhiteList;

  @Value("${metering.ALLOW_ACCESS}")
  private int ALLOW_ACCESS;

  @Value("${metering.ALLOW_BLOCK}")
  private int ALLOW_BLOCK;

  @Value("${metering.DATA_NOT_VALID}")
  private int DATA_NOT_VALID;

  public MeteringService( Clock clock) {
//    this.blockedRepository = blockedRepository;
//    this.blockedTempRepository = blockedTempRepository;
//    this.apiAccessRepository = apiAccessRepository;
//    this.allowedApiRepository = allowedApiRepository;
    this.clock = clock;
  }

  public int checkAccess(MeteringCheckRequest request, String xTid) {

    String serviceNo = request.getServiceNo();
    String handPhoneId = request.getHpId();
    String carId = request.getCarId();
    String requestUrl = request.getReqUrl();

    try {
      Optional<Blocked> blockedCustomer = isCustomerBlocked(handPhoneId, carId);
      if (blockedCustomer.isPresent()) {
        log.warn("[XTID : {}] 미터링 차단된 유저 : CCID[{}], carID[{}]", xTid, handPhoneId, carId);
        return ALLOW_BLOCK;
      }

      String reqUrl = "";
      if (!requestUrl.equals("/pushVersion.do")) {
        reqUrl = requestUrl.substring(requestUrl.indexOf("/ccsp") + 5);
      } else {
        reqUrl = requestUrl;
      }

      // 특정 api에 대하여 carid 가 null이어도 서비스 정상 으로 판단하도록 기능 추가 (taeseong, 21.08.09)
      boolean carIdNullDeny = true;
      if (ObjectUtils.isEmpty(carId)) {
        for (int i = 0; i < remoteControlWhiteList.length; i++) {
          if (reqUrl.equals(remoteControlWhiteList[i])) {
            carIdNullDeny = false;
            break;
          }
        }
        if (carIdNullDeny) {
          return DATA_NOT_VALID;
        }
      }

      if (isAllowedUrl(reqUrl)) {
        return ALLOW_ACCESS;
      }

      //count check
      AccessCheckResult accessCheckResult = shouldHaveAccess(serviceNo, handPhoneId, carId, reqUrl, xTid);

      recordAccess(handPhoneId, carId, reqUrl);

      if (accessCheckResult != AccessCheckResult.SERVICE_SUCCESS) {
        blockCustomer(handPhoneId, carId, accessCheckResult);

        blockCustomerTemp(handPhoneId, carId, accessCheckResult, reqUrl);

        return ALLOW_BLOCK;
      }

      return ALLOW_ACCESS;

    } catch (Exception e) {
      log.error(
        "[XITD : {}] CCSP 미터링 Service [checkAccess] EXCEPTION 발생, serviceNo[{}], CCID[{}], CARID[{}] , Exception : {}",
        xTid, request.getServiceNo(), request.getHpId(), request.getCarId(), e.getMessage());
      throw e;
    }
  }

  private boolean isAllowedUrl(String requestUrl) {
    long foundResultCount = allowedApiRepository.countByRequestUrl(requestUrl);
    return foundResultCount > 0;
  }

  private AccessCheckResult shouldHaveAccess(String serviceNo, String handPhoneId, String carId, String requestUrl,
    String xTid) {

    // [21.10.13 taeseong] 최근 10분간 조회 쿼리 변경
    long attemptsInLast10Minutes = apiAccessRepository.lastTimeAccessCount(
      handPhoneId, carId, requestUrl, OffsetDateTime.now(clock).minusMinutes(10)
    );
    long attemptsToday = apiAccessRepository.dailyAccessCount(handPhoneId, carId, requestUrl);
    if (attemptsInLast10Minutes >= maxTenMinuteAccessCount) {
      log.info("[XTID : {}] CCSP API미터링 차단(1004:10분, 1005:당일), 서비스코드[{}], CCID[{}], CARID[{}], 차단코드[{}]", xTid,
                  serviceNo, handPhoneId, carId, AccessCheckResult.BLOCKED_RSON_10MIN.getResCode());
      return AccessCheckResult.BLOCKED_RSON_10MIN;
    }

    if (attemptsToday >= maxDayAccessCount) {
      log.info("[XTID : {}] CCSP API미터링 차단(1004:10분, 1005:당일), 서비스코드[{}], CCID[{}], CARID[{}], 차단코드[{}]", xTid,
                  serviceNo, handPhoneId, carId, AccessCheckResult.BLOCKED_RSON_DAY.getResCode());
      return AccessCheckResult.BLOCKED_RSON_DAY;
    }

    return AccessCheckResult.SERVICE_SUCCESS;
  }

  private Optional<Blocked> isCustomerBlocked(String handPhoneId, String carId) {
    return blockedRepository.findById(BlockedId.builder()
                                        .handPhoneId(handPhoneId)
                                        .carId(carId)
                                        .build());
  }

  @Async
  public void recordAccess(String handPhoneId, String carId, String requestUrl) {
    apiAccessRepository.save(ApiAccess.builder()
                               .handPhoneId(handPhoneId)
                               .carId(carId)
                               .requestUrl(requestUrl)
                               .accessTime(OffsetDateTime.now(clock))
                               .build());
  }

  @Async
  public void blockCustomer(String handPhoneId, String carId, AccessCheckResult accessCheckResult) {
    blockedRepository.save(Blocked.builder()
                             .handPhoneId(handPhoneId)
                             .carId(carId)
                             .blockedRsonCd(accessCheckResult.getResCode())
                             .blockedTime(OffsetDateTime.now(clock))
                             .build());
  }

  @Async
  public void blockCustomerTemp(String handPhoneId, String carId, AccessCheckResult accessCheckResult,
    String requestUrl) {
    blockedTempRepository.save(BlockedTemp.builder()
                                 .handPhoneId(handPhoneId)
                                 .carId(carId)
                                 .blockedRsonCd(accessCheckResult.getResCode())
                                 .blockedTime(OffsetDateTime.now(clock))
                                 .requestUrl(requestUrl)
                                 .build());
    log.info(
      "Execute method asynchronously. RecordAccess Done : " + Thread.currentThread().getName() + "blockCustomerTemp");
  }

  @AllArgsConstructor
  public enum AccessCheckResult {
    SERVICE_SUCCESS("0000"),
    BLOCKED_RSON_10MIN("1004"),
    BLOCKED_RSON_DAY("1005");

    @Getter
    private final String resCode;
  }

}
