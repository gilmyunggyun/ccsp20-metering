package com.hkmc.ccs.metering.controller;

import com.hkmc.ccs.metering.service.MeteringService;
import com.hkmc.ccs.metering.models.vo.MeteringCheckRequest;
import com.hkmc.ccs.metering.models.vo.MeteringCheckResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.util.Map;

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

    @Value("${metering.ALLOW_ACCESS}")
    private int ALLOW_ACCESS;

    @Value("${metering.ALLOW_BLOCK}")
    private int ALLOW_BLOCK;

    @Value("${metering.DATA_NOT_VALID}")
    private int DATA_NOT_VALID;

    public MeteringController(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @PostMapping("/metering/v1/metering")
    public ResponseEntity<MeteringCheckResponse> checkAPIAccess(@RequestBody MeteringCheckRequest request,
                                                                @RequestHeader Map<String, Object> header
                                                                ) throws Exception {
        String xTid = (String) header.get("XTID");
        try {
            // @Validation을 사용하는 경우, MeteringCheckResult 내 carId 또한 notBlank/notNull/notEmpty로 null check가 이루어져, 부득이하게 각 컨텐츠별 isEmpty를 이용하여 validation 진행 (taeseong, 21.08.09)
            if(ObjectUtils.isEmpty(request.getServiceNo()) ||
                    ObjectUtils.isEmpty(request.getHpId()) ||
                            ObjectUtils.isEmpty(request.getReqUrl())
            ){
                LOGGER.warn("[XTID : {}] 미터링 ValidationCheck 전문형식오류 서비스ID[{}] carID[{}] CCID[{}] requestURL[{}]",xTid,request.getServiceNo(),request.getCarId(),request.getHpId(),request.getReqUrl());
                return status(BAD_REQUEST).body(MeteringCheckResponse.builder()
                        .serviceNo(request.getServiceNo())
                        .retCode(result_fail)
                        .resCode(MSG_FORMAT_INVALID)
                        .build());
            }

            int hasAccess = meteringService.checkAccess(request,xTid);

            // 비즈니스단에서 url에 따른 carId 사용가능여부 판단을 위해 boolean -> int/if 로 변경 (taeseong, 21.08.09)
            if(hasAccess == ALLOW_BLOCK) {
                return status(TOO_MANY_REQUESTS).body(
                        MeteringCheckResponse.builder()
                                .serviceNo(request.getServiceNo())
                                .retCode(result_fail)
                                .resCode(BLOCK_BY_API)
                                .build());
            }
            else if (hasAccess == DATA_NOT_VALID) {
                LOGGER.warn("[XTID : {}] 미터링 ValidationCheck carID null 오류 서비스ID[{}] carID[{}] CCID[{}] requestURL[{}]",xTid,request.getServiceNo(),request.getCarId(),request.getHpId(),request.getReqUrl());
                return status(BAD_REQUEST).body(MeteringCheckResponse.builder()
                        .serviceNo(request.getServiceNo())
                        .retCode(result_fail)
                        .resCode(MSG_FORMAT_INVALID)
                        .build());
            }

            return ok(MeteringCheckResponse.builder()
                    .serviceNo(request.getServiceNo())
                    .retCode(result_success)
                    .resCode(SERVICE_SUCCESS)
                    .build());

        } catch (Exception e) {
            LOGGER.warn("[XTID : {}] CCSP 미터링 Controller EXCEPTION 발생, serviceNo[{}], CCID[{}], CARID[{}] ,EXCEPTION : {}",xTid,request.getServiceNo(),request.getHpId(),request.getCarId(),e.getMessage());
            throw e;
        }
    }
}
