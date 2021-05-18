package com.hkmc.ccs.metering.service;

import com.hkmc.ccs.metering.models.entity.ApiAccess;
import com.hkmc.ccs.metering.models.entity.Blocked;
import com.hkmc.ccs.metering.models.entity.BlockedId;
import com.hkmc.ccs.metering.models.entity.BlockedTemp;
import com.hkmc.ccs.metering.models.vo.MeteringCheckRequest;
import com.hkmc.ccs.metering.repository.AllowedApiRepository;
import com.hkmc.ccs.metering.repository.ApiAccessRepository;
import com.hkmc.ccs.metering.repository.BlockedRepository;
import com.hkmc.ccs.metering.repository.BlockedTempRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;

@Slf4j
@Service
public class MeteringService {

    @AllArgsConstructor
    public enum AccessCheckResult {
        SERVICE_SUCCESS("0000"),
        BLOCKED_RSON_10MIN("1004"),
        BLOCKED_RSON_DAY("1005");

        @Getter private final String resCode;
    }

    @Value("${metering.max-ten-minute-access-count}")
    private int maxTenMinuteAccessCount = 200;

    @Value("${metering.max-day-access-count}")
    private int maxDayAccessCount = 300;

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MeteringService.class);
    private final BlockedRepository blockedRepository;
    private final BlockedTempRepository blockedTempRepository;
    private final ApiAccessRepository apiAccessRepository;
    private final AllowedApiRepository allowedApiRepository;
    private final Clock clock;

    public MeteringService(BlockedRepository blockedRepository, BlockedTempRepository blockedTempRepository, ApiAccessRepository apiAccessRepository, AllowedApiRepository allowedApiRepository, Clock clock) {
        this.blockedRepository = blockedRepository;
        this.blockedTempRepository = blockedTempRepository;
        this.apiAccessRepository = apiAccessRepository;
        this.allowedApiRepository = allowedApiRepository;
        this.clock = clock;
    }

    public boolean checkAccess(MeteringCheckRequest request,String xTid) {

        String serviceNo = request.getServiceNo();
        String handPhoneId = request.getHpId();
        String carId = request.getCarId();
        String requestUrl = request.getReqUrl();

        try {
            Optional<Blocked> blockedCustomer = isCustomerBlocked(handPhoneId, carId);
            if (blockedCustomer.isPresent()) {
                LOGGER.warn("[XTID : {}] 미터링 차단된 유저 : CCID[{}], carID[{}]",xTid, handPhoneId, carId);
                return false;
            }

            String reqUrl = "";
            if(!requestUrl.equals("/pushVersion.do")){
                reqUrl = requestUrl.substring(requestUrl.indexOf("/ccsp")+5,requestUrl.length());
            }else{
                reqUrl = requestUrl;
            }

            if (isAllowedUrl(reqUrl)) {
                return true;
            }

            //count check
            AccessCheckResult accessCheckResult = shouldHaveAccess(serviceNo, handPhoneId, carId, reqUrl, xTid);

            recordAccess(handPhoneId, carId, reqUrl);

            if (accessCheckResult != AccessCheckResult.SERVICE_SUCCESS) {
                blockCustomer(handPhoneId, carId, accessCheckResult);

                blockCustomerTemp(handPhoneId, carId, accessCheckResult, reqUrl);

                return false;
            }

            return true;

        } catch (Exception e) {
            LOGGER.warn("[XITD : {}] CCSP 미터링 Service [checkAccess] EXCEPTION 발생, serviceNo[{}], CCID[{}], CARID[{}] , Exception : {}",xTid,request.getServiceNo(),request.getHpId(),request.getCarId(),e.getMessage());
            throw e;
        }
    }

    private boolean isAllowedUrl(String requestUrl) {
        long foundResultCount = allowedApiRepository.countByRequestUrl(requestUrl);

        return foundResultCount > 0;
    }

    private AccessCheckResult shouldHaveAccess(String serviceNo, String handPhoneId, String carId, String requestUrl, String xTid) {

        long attemptsInLast10Minutes = apiAccessRepository.countByHandPhoneIdAndCarIdAndRequestUrlAndAccessTimeAfter(
                handPhoneId, carId, requestUrl, OffsetDateTime.now(clock).minusMinutes(10)
        );
        long attemptsToday = apiAccessRepository.dailyAccessCount(handPhoneId, carId, requestUrl);
        if (attemptsInLast10Minutes >= maxTenMinuteAccessCount) {
            LOGGER.info("[XTID : {}] CCSP API미터링 차단(1004:10분, 1005:당일), 서비스코드[{}], CCID[{}], CARID[{}], 차단코드[{}]",xTid,serviceNo,handPhoneId,carId,AccessCheckResult.BLOCKED_RSON_10MIN.getResCode());
            return AccessCheckResult.BLOCKED_RSON_10MIN;
        }

        if (attemptsToday >= maxDayAccessCount) {
            LOGGER.info("[XTID : {}] CCSP API미터링 차단(1004:10분, 1005:당일), 서비스코드[{}], CCID[{}], CARID[{}], 차단코드[{}]",xTid,serviceNo,handPhoneId,carId,AccessCheckResult.BLOCKED_RSON_DAY.getResCode());
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
    public void blockCustomerTemp(String handPhoneId, String carId, AccessCheckResult accessCheckResult, String requestUrl) {
        blockedTempRepository.save(BlockedTemp.builder()
                .handPhoneId(handPhoneId)
                .carId(carId)
                .blockedRsonCd(accessCheckResult.getResCode())
                .blockedTime(OffsetDateTime.now(clock))
                .requestUrl(requestUrl)
                .build());
        log.info("Execute method asynchronously. RecordAccess Done : " + Thread.currentThread().getName() + "blockCustomerTemp");
    }
}
