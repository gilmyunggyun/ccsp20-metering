package com.hyundaiautoever.ccs.metering;

import com.hyundaiautoever.ccs.metering.VO.MeteringCheckRequest;
import lombok.Builder;
import lombok.Data;
import org.hibernate.dialect.pagination.AbstractLimitHandler;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class MeteringService {

    @Value("${metering.max-ten-minute-access-count}")
    private int maxTenMinuteAccessCount = 200;

    @Value("${metering.max-day-access-count}")
    private int maxDayAccessCount = 300;

    @Value("${RETURN_CODE_TYPE.FAIL}")
    private String result_fail = "F";

    @Value("${RETURN_CODE_TYPE.SUCCESS}")
    private String result_success = "S";

    @Value("${RETURN_CODE_TYPE.BLOCKED}")
    private String result_blocked = "B";

    @Value("${metering.BLOCK_BY_API}")
    private String BLOCK_BY_API = "BK02";

    @Value("${metering.SERVICE_SUCCESS}")
    private String SERVICE_SUCCESS = "0000";

    @Value("${metering.MSG_FORMAT_INVALID}")
    private String MSG_FORMAT_INVALID = "S999";

    @Value("${metering.BLOCKED_RSON_DAY}")
    private String BLOCKED_RSON_DAY = "1005";

    @Value("${metering.BLOCKED_RSON_10MIN}")
    private String BLOCKED_RSON_10MIN = "1004";


    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MeteringService.class);
    private final BlockedRepository blockedRepository;
    private final ApiAccessRepository apiAccessRepository;
    private final AllowedApiRepository allowedApiRepository;
    private final Clock clock;

    private final AccessCheckResult success = AccessCheckResult.builder()
            .RetCode(result_success)
            .resCode(SERVICE_SUCCESS)
            .build();

    private final AccessCheckResult blocked = AccessCheckResult.builder()
            .RetCode(result_blocked)
            .resCode(BLOCK_BY_API)
            .build();

    public MeteringService(BlockedRepository blockedRepository, ApiAccessRepository apiAccessRepository, AllowedApiRepository allowedApiRepository, Clock clock) {
        this.blockedRepository = blockedRepository;
        this.apiAccessRepository = apiAccessRepository;
        this.allowedApiRepository = allowedApiRepository;
        this.clock = clock;
    }

    public AccessCheckResult checkAccess(MeteringCheckRequest request) {

        String serviceNo = request.getServiceNo();
        String handPhoneId = request.getHpId();
        String carId = request.getCarId();
        String requestUrl = request.getReqUrl();

        try {
            Optional<Blocked> blockedCustomer = isCustomerBlocked(handPhoneId, carId);
            if (blockedCustomer.isPresent()) {
                LOGGER.warn("미터링 차단된 유저 : CCID[" + handPhoneId + "] carID[" + carId + "]");
                return blocked;
            }

            if (isAllowedUrl(requestUrl)) {
                return success;
            }

            //count check
            AccessCheckResult shouldHaveAccess = shouldHaveAccess(serviceNo, handPhoneId, carId, requestUrl);

            recordAccess(handPhoneId, carId, requestUrl);

            if (!shouldHaveAccess.getRetCode().equals(result_success)) {
                String blockedRsonCd = shouldHaveAccess.getResCode();
                blockCustomer(handPhoneId, carId, blockedRsonCd);

                return blocked;
            }

            return success;

        } catch (Exception e) {
            LOGGER.warn("CCSP 미터링 Service [checkAccess] EXCEPTION 발생, serviceNo[\"" + request.getServiceNo() + "\"], CCID[\"" + request.getHpId() + "\"], CARID[\"" + request.getCarId() + "]  에러[" + getExceptionDetailMsg(e) + "]");
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
            LOGGER.info("CCSP API미터링 차단(1004:10분, 1005:당일), 서비스코드[" + serviceNo + "], CCID[" + handPhoneId + "], CARID[" + carId + "], 차단코드[" + BLOCKED_RSON_10MIN + "] ");
            return AccessCheckResult.builder().resCode(BLOCKED_RSON_10MIN).RetCode(result_fail).build();
        }

        if (attemptsToday >= maxDayAccessCount) {
            LOGGER.info("CCSP API미터링 차단(1004:10분, 1005:당일), 서비스코드[" + serviceNo + "], CCID[" + handPhoneId + "], CARID[" + carId + "], 차단코드[" + BLOCKED_RSON_DAY + "] ");
            return AccessCheckResult.builder().resCode(BLOCKED_RSON_DAY).RetCode(result_fail).build();
        }

        return AccessCheckResult.builder()
                .RetCode(result_success)
                .resCode(SERVICE_SUCCESS)
                .build();
    }

    private Optional<Blocked> isCustomerBlocked(String handPhoneId, String carId) {
        Optional<Blocked> maybeBlocked = blockedRepository.findById(BlockedId.builder()
                .handPhoneId(handPhoneId)
                .carId(carId)
                .build());

        return maybeBlocked;
    }

    private void recordAccess(String handPhoneId, String carId, String requestUrl) {
        apiAccessRepository.save(ApiAccess.builder()
                .handPhoneId(handPhoneId)
                .carId(carId)
                .requestUrl(requestUrl)
                .accessTime(OffsetDateTime.now(clock))
                .build());
    }

    private void blockCustomer(String handPhoneId, String carId, String blockedRsonCd) {
        blockedRepository.save(Blocked.builder()
                .handPhoneId(handPhoneId)
                .carId(carId)
                .blockedRsonCd(blockedRsonCd)
                .blockedTime(OffsetDateTime.now(clock))
                .build());
    }

    public String getExceptionDetailMsg(Exception e) {

        StringBuffer sbErrMsg = new StringBuffer();

        StackTraceElement[] elem = e.getStackTrace();

        for (int i = 0; i < elem.length; i++) {
            sbErrMsg.append(elem[i]);
            sbErrMsg.append("\n");
        }

        return sbErrMsg.toString();
    }

    @Data
    @Builder
    public static class AccessCheckResult {
        String RetCode;
        String resCode;
    }
}
