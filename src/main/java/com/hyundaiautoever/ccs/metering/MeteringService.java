package com.hyundaiautoever.ccs.metering;

import com.hyundaiautoever.ccs.metering.VO.MeteringCheckRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;

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
    private final ApiAccessRepository apiAccessRepository;
    private final AllowedApiRepository allowedApiRepository;
    private final Clock clock;

    public MeteringService(BlockedRepository blockedRepository, ApiAccessRepository apiAccessRepository, AllowedApiRepository allowedApiRepository, Clock clock) {
        this.blockedRepository = blockedRepository;
        this.apiAccessRepository = apiAccessRepository;
        this.allowedApiRepository = allowedApiRepository;
        this.clock = clock;
    }

    public boolean checkAccess(MeteringCheckRequest request) {

        String serviceNo = request.getServiceNo();
        String handPhoneId = request.getHpId();
        String carId = request.getCarId();
        String requestUrl = request.getReqUrl();

        try {
            Optional<Blocked> blockedCustomer = isCustomerBlocked(handPhoneId, carId);
            if (blockedCustomer.isPresent()) {
                LOGGER.warn("미터링 차단된 유저 : CCID[" + handPhoneId + "] carID[" + carId + "]");
                return false;
            }

            if (isAllowedUrl(requestUrl)) {
                return true;
            }

            //count check
            AccessCheckResult accessCheckResult = shouldHaveAccess(serviceNo, handPhoneId, carId, requestUrl);

            recordAccess(handPhoneId, carId, requestUrl);

            if (accessCheckResult != AccessCheckResult.SERVICE_SUCCESS) {
                blockCustomer(handPhoneId, carId, accessCheckResult);

                return false;
            }

            return true;

        } catch (Exception e) {
            LOGGER.warn("CCSP 미터링 Service [checkAccess] EXCEPTION 발생, serviceNo[\"" + request.getServiceNo() + "\"], CCID[\"" + request.getHpId() + "\"], CARID[\"" + request.getCarId() + "]", e);
            throw e;
        }
    }

    private boolean isAllowedUrl(String requestUrl) {
        long foundResultCount = allowedApiRepository.countByRequestUrl(requestUrl);

        return foundResultCount > 0;
    }

    private AccessCheckResult shouldHaveAccess(String serviceNo, String handPhoneId, String carId, String requestUrl) {

        long attemptsInLast10Minutes = apiAccessRepository.countByHandPhoneIdAndCarIdAndRequestUrlAndAccessTimeAfter(
                handPhoneId, carId, requestUrl, OffsetDateTime.now(clock).minusMinutes(10)
        );

        long attemptsToday = apiAccessRepository.dailyAccessCount(handPhoneId, carId, requestUrl);

        if (attemptsInLast10Minutes >= maxTenMinuteAccessCount) {
            LOGGER.info("CCSP API미터링 차단(1004:10분, 1005:당일), 서비스코드[" + serviceNo + "], CCID[" + handPhoneId + "], CARID[" + carId + "], 차단코드[" + AccessCheckResult.BLOCKED_RSON_10MIN.getResCode() + "] ");
            return AccessCheckResult.BLOCKED_RSON_10MIN;
        }

        if (attemptsToday >= maxDayAccessCount) {
            LOGGER.info("CCSP API미터링 차단(1004:10분, 1005:당일), 서비스코드[" + serviceNo + "], CCID[" + handPhoneId + "], CARID[" + carId + "], 차단코드[" + AccessCheckResult.BLOCKED_RSON_DAY.getResCode() + "] ");
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

    private void recordAccess(String handPhoneId, String carId, String requestUrl) {
        apiAccessRepository.save(ApiAccess.builder()
                .handPhoneId(handPhoneId)
                .carId(carId)
                .requestUrl(requestUrl)
                .accessTime(OffsetDateTime.now(clock))
                .build());
    }

    private void blockCustomer(String handPhoneId, String carId, AccessCheckResult accessCheckResult) {
        blockedRepository.save(Blocked.builder()
                .handPhoneId(handPhoneId)
                .carId(carId)
                .blockedRsonCd(accessCheckResult.getResCode())
                .blockedTime(OffsetDateTime.now(clock))
                .build());
    }
}
