package com.hyundaiautoever.ccs.metering;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
                .ServiceNo(request.getServiceNo());

        if (!hasAccess) {
            return status(TOO_MANY_REQUESTS).body(
                    responseBuilder.ServiceNo(request.getServiceNo()).retCode("F").resCode("BK02").build()
            );
        }

        return ok(responseBuilder.ServiceNo(request.getServiceNo()).retCode("S").resCode("S000").build());
    }


    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class MeteringCheckRequest {
        @JsonProperty("ServiceNo")
        private String serviceNo;

        @JsonProperty("CCID")
        private String hpId;

        @JsonProperty("carID")
        private String carId;

        private String reqUrl;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class MeteringCheckResponse {
        @JsonProperty("serviceNo")
        private String ServiceNo;
        private String retCode;
        private String resCode;
    }
}
