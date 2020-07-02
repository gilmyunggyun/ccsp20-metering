package com.hyundaiautoever.ccs.metering;

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

    public boolean checkAccess(String handPhoneId, String carId, String requestUrl) {
        if (isCustomerBlocked(handPhoneId, carId)) {
            return false;
        }

        if (isAllowedUrl(requestUrl)) {
            return true;
        }

        boolean shouldHaveAccess = shouldHaveAccess(handPhoneId, carId, requestUrl);

        recordAccess(handPhoneId, carId, requestUrl);

        if (!shouldHaveAccess) {
            blockCustomer(handPhoneId, carId);
        }

        return shouldHaveAccess;
    }

    private boolean isAllowedUrl(String requestUrl) {
        long foundResultCount = allowedApiRepository.countByRequestUrl(requestUrl);

        return foundResultCount > 0;
    }

    private boolean shouldHaveAccess(String handPhoneId, String carId, String requestUrl) {
        long attemptsInLast10Minutes = apiAccessRepository.countByHandPhoneIdAndCarIdAndRequestUrlAndAccessTimeAfter(
                handPhoneId, carId, requestUrl, OffsetDateTime.now(clock).minusMinutes(10)
        );

        long attemptsToday = apiAccessRepository.dailyAccessCount(handPhoneId, carId, requestUrl);

        return attemptsInLast10Minutes < maxTenMinuteAccessCount
                && attemptsToday < maxDayAccessCount;
    }

    private boolean isCustomerBlocked(String handPhoneId, String carId) {
        Optional<Blocked> maybeBlocked = blockedRepository.findById(BlockedId.builder()
                .handPhoneId(handPhoneId)
                .carId(carId)
                .build());

        return maybeBlocked.isPresent();
    }

    private void recordAccess(String handPhoneId, String carId, String requestUrl) {
        apiAccessRepository.save(ApiAccess.builder()
                .handPhoneId(handPhoneId)
                .carId(carId)
                .requestUrl(requestUrl)
                .accessTime(OffsetDateTime.now(clock))
                .build());
    }

    private void blockCustomer(String handPhoneId, String carId) {
        blockedRepository.save(Blocked.builder()
                .handPhoneId(handPhoneId)
                .carId(carId)
                .blockedTime(OffsetDateTime.now(clock))
                .build());
    }
}
