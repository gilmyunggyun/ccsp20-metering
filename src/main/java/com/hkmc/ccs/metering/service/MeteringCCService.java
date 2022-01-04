package com.hkmc.ccs.metering.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import com.hkmc.ccs.metering.models.entity.Blocked;
import com.hkmc.ccs.metering.models.entity.Unblocked;
import com.hkmc.ccs.metering.models.vo.MeteringCCBlockList;
import com.hkmc.ccs.metering.models.vo.MeteringCCRequest;
import com.hkmc.ccs.metering.repository.BlockedRepository;
import com.hkmc.ccs.metering.repository.UnblockedRepository;

/***
 *  콜센터 to 미터링 api 서비스
 */
@Slf4j
@Service
public class MeteringCCService {

  private final UnblockedRepository unblockedRepository;

  private final BlockedRepository blockedRepository;

  private final Clock clock;

  private final String BLOCKED_RSON_10MIN = "1004";

  private final String BLOCKED_RSON_DAY = "1005";

  public MeteringCCService(UnblockedRepository unblockedRepository, BlockedRepository blockedRepository, Clock clock) {
    this.unblockedRepository = unblockedRepository;
    this.blockedRepository = blockedRepository;
    this.clock = clock;
  }

  public List<MeteringCCBlockList> getBlockList(MeteringCCRequest request) {

    String carId = request.getCarId();

    try {
      List<Blocked> list = findAllByCarId(carId);

      if (ObjectUtils.isEmpty(list)) {
        log.warn("미터링 CC getBloickList 데이터 없음 carID[{}]", request.getCarId());
        return null;
      }

      List<MeteringCCBlockList> blockList = new ArrayList<>();
      MeteringCCBlockList meteringCCBlockList;

      for (Blocked blocked : list) {
        // init
        meteringCCBlockList = new MeteringCCBlockList();

        // 핸드폰id
        meteringCCBlockList.setCcid(blocked.getHandPhoneId());
        // 차단일시
        meteringCCBlockList.setBlockedDate(
          blocked.getBlockedTime().format(DateTimeFormatter.ofPattern("yyyMMddHHmmss")));
        // 차단 사유
        if (blocked.getBlockedRsonCd().equals(BLOCKED_RSON_10MIN)) {
          meteringCCBlockList.setRsonCd("10분 한도초과");
        } else if (blocked.getBlockedRsonCd().equals(BLOCKED_RSON_DAY)) {
          meteringCCBlockList.setRsonCd("일 한도 초과");
        } else {
          meteringCCBlockList.setRsonCd("오류");
        }
        // List에 추가
        blockList.add(meteringCCBlockList);
      }

      return blockList;
    } catch (Exception e) {
      log.error("미터링 CC getBloickList Exception 발생 carID[{}]", request.getCarId());
      throw e;
    }
  }

  public int unblock(MeteringCCRequest request) {

    String carId = request.getCarId();
    String requestId = request.getRequestId();
    try {
      List<Blocked> list = findAllByCarId(carId);

      // 삭제할 데이터 없음
      if (ObjectUtils.isEmpty(list)) {
        log.warn("미터링 CC getBloickList 데이터 없음 carID[{}]", request.getCarId());
        return 0;
      }

      // 삭제할 데이터 이력 저장
      for (Blocked blocked : list) {
        unblockRequest(blocked.getHandPhoneId()
          , blocked.getCarId()
          , blocked.getBlockedRsonCd()
          , blocked.getBlockedTime()
          , requestId);
      }

      // 삭제진행
      int result = blockedRepository.deleteByCarId(carId);

      log.debug("미터링 CC unblock 삭제 {} row, carID[{}]", result, request.getCarId());

      return result;
    } catch (Exception e) {
      log.error("미터링 CC unblock Exception 발생 carID[{}]", request.getCarId());
      throw e;
    }
  }

  private List<Blocked> findAllByCarId(String carId) {
    return blockedRepository.findAllByCarId(carId);
  }

  @Async
  public void unblockRequest(String handPhoneId, String carId, String rsonCd, OffsetDateTime blockedTime,
    String requestId) {
    unblockedRepository.save(Unblocked.builder()
                               .handPhoneId(handPhoneId)
                               .carId(carId)
                               .blockedRsonCd(rsonCd)
                               .blockedTime(blockedTime)
                               .requestId(requestId)
                               .requestTime(OffsetDateTime.now())
                               .build());
  }

}
