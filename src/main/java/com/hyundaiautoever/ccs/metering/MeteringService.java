package com.hyundaiautoever.ccs.metering;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MeteringService {

    private final BlockedRepository repository;

    public MeteringService(BlockedRepository repository) {
        this.repository = repository;
    }

    public boolean checkAccess(String serviceNumber, String handPhoneId, String carId, String requestUrl) {

        Optional<Blocked> maybeBlocked = repository.findById(BlockedId.builder()
                .handPhoneId(handPhoneId)
                .carId(carId)
                .build());

        if (maybeBlocked.isPresent()) {
            return false;
        }

        return true;

        //duplicate check

        //delete duplicated data

        //select isol count

        //isol > 0 , return

        //select metering detail info

        //if detail info result = null  then insert block DB

        //if not null,
            //set timeIntervalYn , dayInterverYn

            //update metering data

            //select time metering standard count

            //select day metering standard count

            //select exception metering service count

                //if cnt overs, insert isol table




    }
}
