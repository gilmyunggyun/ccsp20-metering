package com.hyundaiautoever.ccs.metering;

import com.hyundaiautoever.ccs.metering.models.vo.MeteringCheckRequest;
import com.hyundaiautoever.ccs.metering.models.vo.MeteringCheckResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
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
    public ResponseEntity<MeteringCheckResponse> checkAPIAccess(@Valid @RequestBody MeteringCheckRequest request,
                                                                BindingResult result) throws Exception {

        try {
            if (result.hasErrors()) {
                LOGGER.warn("미터링 ValidationCheck 전문형식오류 서비스ID[" + request.getServiceNo() + "] carID[" + request.getCarId() + "] CCID[" + request.getHpId() + "] requestURL[" + request.getReqUrl() + "]");

                return status(BAD_REQUEST).body(MeteringCheckResponse.builder()
                        .serviceNo(request.getServiceNo())
                        .retCode(result_fail)
                        .resCode(MSG_FORMAT_INVALID)
                        .build());
            }

            boolean hasAccess = meteringService.checkAccess(request);

            if (!hasAccess) {
                return status(TOO_MANY_REQUESTS).body(
                        MeteringCheckResponse.builder()
                                .serviceNo(request.getServiceNo())
                                .retCode(result_fail)
                                .resCode(BLOCK_BY_API)
                                .build()
                );
            }

            return ok(MeteringCheckResponse.builder()
                    .serviceNo(request.getServiceNo())
                    .retCode(result_success)
                    .resCode(SERVICE_SUCCESS)
                    .build());

        } catch (Exception e) {
            LOGGER.warn("CCSP 미터링 Controller EXCEPTION 발생, serviceNo[\"" + request.getServiceNo() + "\"], CCID[\"" + request.getHpId() + "\"], CARID[\"" + request.getCarId() + "]", e);
            throw e;
        }
    }
}
