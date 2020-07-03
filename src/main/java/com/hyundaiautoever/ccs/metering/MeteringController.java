package com.hyundaiautoever.ccs.metering;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
public class MeteringController {

    private final MeteringService meteringService;

    public MeteringController(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @PostMapping("/metering")
    public ResponseEntity<MeteringCheckResponse> checkAPIAccess(@RequestBody MeteringCheckRequest request) {

        boolean hasAccess = meteringService.checkAccess(
                request.getHpId(),
                request.getCarId(),
                request.getReqUrl()
        );

        MeteringCheckResponse.MeteringCheckResponseBuilder responseBuilder = MeteringCheckResponse.builder()
                .serviceNo(request.getServiceNo());

        if (!hasAccess) {
            return status(TOO_MANY_REQUESTS).body(
                    responseBuilder.retCode("F").resCode("BK02").build()
            );
        }

        return ok(responseBuilder.retCode("S").resCode("S000").build());
    }

    @Data
    public static class MeteringCheckRequest {
        private String serviceNo;
        private String hpId;
        private String carId;
        private String reqUrl;
    }

    @Builder
    @Data
    public static class MeteringCheckResponse {
        private String serviceNo;
        private String retCode;
        private String resCode;
    }
}
