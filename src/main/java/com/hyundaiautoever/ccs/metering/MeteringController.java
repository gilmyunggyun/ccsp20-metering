package com.hyundaiautoever.ccs.metering;

import com.hyundaiautoever.ccs.metering.VO.MeteringCheckRequest;
import com.hyundaiautoever.ccs.metering.VO.MeteringCheckResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
public class MeteringController {

    private final MeteringService meteringService;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MeteringController.class);


    @Value("${RETURN_CODE_TYPE.FAIL}")
    private String result_fail;

    @Value("${RETURN_CODE_TYPE.SUCCESS}")
    private String result_success;

    @Value("${metering.BLOCK_BY_API}")
    private String BLOCK_BY_API;

    @Value("${metering.SERVICE_SUCCESS}")
    private String SERVICE_SUCCESS;

    @Value("${metering.MSG_FORMAT_INVALID}")
    private String MSG_FORMAT_INVALID;

    public MeteringController(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @PostMapping("/metering")
    public ResponseEntity<MeteringCheckResponse> checkAPIAccess(@Valid @RequestBody MeteringCheckRequest request) throws Exception {

        //TODO: Need to Add circuitBreaker --> for the 500 error etc

        try {
            //validation check
            if (request.getServiceNo().isEmpty() || request.getCarId().isEmpty() || request.getHpId().isEmpty() || request.getReqUrl().isEmpty()) {

                LOGGER.warn("미터링 ValidationCheck 전문형식오류 서비스ID[" + request.getServiceNo() + "] carID[" + request.getCarId() + "] CCID[" + request.getHpId() + "] requestURL[" + request.getReqUrl() + "]");

                return status(400).body(MeteringCheckResponse.builder()
                        .RetCode(result_fail)
                        .resCode(MSG_FORMAT_INVALID)
                        .ServiceNo(request.getServiceNo())
                        .build());
            }

            //accessCheck
            MeteringCheckResponse hasAccess = meteringService.checkAccess(request);

            String hassAccessResCode = hasAccess.getResCode();

            if (hassAccessResCode.equals(BLOCK_BY_API)) {
                return status(TOO_MANY_REQUESTS).body(
                        MeteringCheckResponse.builder()
                                .RetCode(result_fail)
                                .ServiceNo(hasAccess.getServiceNo())
                                .resCode(hasAccess.getResCode())
                                .build()
                );

            }

            if (!hassAccessResCode.equals(SERVICE_SUCCESS)) {
                //circuit breaker?
                return status(SERVICE_UNAVAILABLE).body(
                        MeteringCheckResponse.builder()
                                .RetCode(result_fail)
                                .resCode("Server Error")
                                .build());

            }
            return ok(hasAccess);

        } catch (Exception e) {
            LOGGER.warn("CCSP 미터링 Controller EXCEPTION 발생, serviceNo[\"" + request.getServiceNo() + "\"], CCID[\"" + request.getHpId() + "\"], CARID[\"" + request.getCarId() + "]  에러[" + getExceptionDetailMsg(e) + "]");
            throw e;
        }


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

}
