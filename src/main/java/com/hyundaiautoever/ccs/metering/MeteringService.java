package com.hyundaiautoever.ccs.metering;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class MeteringService {

    private final BlockedRepository blockedRepository;
    private final ApiAccessRepository apiAccessRepository;
    private final Clock clock;

    public MeteringService(BlockedRepository blockedRepository, ApiAccessRepository apiAccessRepository, Clock clock) {
        this.blockedRepository = blockedRepository;
        this.apiAccessRepository = apiAccessRepository;
        this.clock = clock;
    }

    public boolean checkAccess(String serviceNumber, String handPhoneId, String carId, String requestUrl) {

        Optional<Blocked> maybeBlocked = blockedRepository.findById(BlockedId.builder()
                .handPhoneId(handPhoneId)
                .carId(carId)
                .build());

        if (maybeBlocked.isPresent()) {
            return false;
        }

        apiAccessRepository.save(ApiAccess.builder()
                .handPhoneId(handPhoneId)
                .carId(carId)
                .requestUrl(requestUrl)
                .accessTime(OffsetDateTime.now(clock))
                .build());

        return true;


        //select metering detail info


        //if detail info result = null  then insert checkAccess DB

        //if not null,
        //set timeIntervalYn , dayInterverYn

        //update metering data

        //select time metering standard count

        //select day metering standard count

        //select exception metering service count

        //if cnt overs, insert isol table


    }
}
