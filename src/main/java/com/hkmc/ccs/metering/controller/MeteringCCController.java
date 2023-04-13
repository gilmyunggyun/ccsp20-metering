package com.hkmc.ccs.metering.controller;

import java.util.List;
import java.util.Map;
//import jakarta.validation.Valid;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.LoggerFactory;

import com.hkmc.ccs.metering.models.vo.MeteringCCBlockList;
import com.hkmc.ccs.metering.models.vo.MeteringCCRequest;
import com.hkmc.ccs.metering.models.vo.MeteringCCResponse;
import com.hkmc.ccs.metering.service.MeteringCCService;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

/***
 *  콜센터 to 미터링 api 서비스
 */
@RestController
public class MeteringCCController {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MeteringController.class);

  private final MeteringCCService meteringCCService;

  @Value("${RETURN_CODE_TYPE.FAIL}")
  private String result_fail = "F";

  @Value("${RETURN_CODE_TYPE.SUCCESS}")
  private String result_success = "S";

  public MeteringCCController(MeteringCCService meteringCCService) {
    this.meteringCCService = meteringCCService;
  }

  @PostMapping("/metering/v1/getBlockList")
  public ResponseEntity<MeteringCCResponse> getBlockList(@Valid @RequestBody MeteringCCRequest request,
    BindingResult result,
    @RequestHeader Map<String, Object> header) throws Exception {
    try {
      if (result.hasErrors()) {
        LOGGER.warn("미터링 CC getBloickList 전문형식오류 carID[{}]", request.getCarId());
        return status(BAD_REQUEST).body(MeteringCCResponse.builder()
                                          .resultCode(result_fail)
                                          .resultMessage("REQUEST ERROR(형식오류)")
                                          .carId(request.getCarId())
                                          .build());
      }

      // DB 리스트 조회
      List<MeteringCCBlockList> blockList = meteringCCService.getBlockList(request);
      if (ObjectUtils.isEmpty(blockList)) {
        LOGGER.warn("미터링 CC getBloickList 조회 정보 없음 carID[{}], requestID[{}]", request.getCarId(), request.getRequestId());
        return status(BAD_REQUEST).body(MeteringCCResponse.builder()
                                          .resultCode(result_fail)
                                          .resultMessage("조회 정보 없음")
                                          .carId(request.getCarId())
                                          .build());
      }

      return ok(MeteringCCResponse.builder()
                  .resultCode(result_success)
                  .resultMessage("Success")
                  .carId(request.getCarId())
                  .blockList(blockList)
                  .build());
    } catch (Exception e) {
      LOGGER.error("[XTID : {}] carID[{}] " + e.getMessage(),
              header.get("xtid"),
              request.getCarId());

      return status(INTERNAL_SERVER_ERROR).body(MeteringCCResponse.builder()
                                                  .resultCode(result_fail)
                                                  .resultMessage("Internal Server Error")
                                                  .carId(request.getCarId())
                                                  .build());
    }
  }

  @PostMapping("/metering/v1/unblock")
  public ResponseEntity<MeteringCCResponse> unblock(@Valid @RequestBody MeteringCCRequest request,
    BindingResult result,
    @RequestHeader Map<String, Object> header) throws Exception {

    try {
      if (result.hasErrors()) {
        LOGGER.warn("미터링 CC unblock 전문형식오류 carID[{}]", request.getCarId());
        return status(BAD_REQUEST).body(MeteringCCResponse.builder()
                                          .resultCode(result_fail)
                                          .resultMessage("REQUEST ERROR(형식오류)")
                                          .carId(request.getCarId())
                                          .build());
      }
      // 데이터삭제
      int count = meteringCCService.unblock(request);

      if (count == 0) {
        return status(BAD_REQUEST).body(MeteringCCResponse.builder()
                                          .resultCode(result_fail)
                                          .resultMessage("삭제할 정보 없음")
                                          .carId(request.getCarId())
                                          .build());
      }

      return ok(MeteringCCResponse.builder()
                  .resultCode(result_success)
                  .resultMessage("Success")
                  .carId(request.getCarId())
                  .build());
    } catch (Exception e) {
      LOGGER.error("[XTID : {}] carID[{}] " + e.getMessage(),
              header.get("xtid"),
              request.getCarId());
      return status(INTERNAL_SERVER_ERROR).body(MeteringCCResponse.builder()
                                                  .resultCode(result_fail)
                                                  .resultMessage("Internal Server Error")
                                                  .carId(request.getCarId())
                                                  .build());
    }
  }

}
