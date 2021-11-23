package com.hkmc.ccs.metering.service;

import com.hkmc.ccs.metering.models.entity.Blocked;
import com.hkmc.ccs.metering.models.vo.MeteringCCBlockList;
import com.hkmc.ccs.metering.models.vo.MeteringCCRequest;
import com.hkmc.ccs.metering.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RefreshScope
class MeteringCCServiceTest {

    private UnblockedRepository unblockedRepository;
    private BlockedRepository blockedRepository;
    private Clock clock;

    private MeteringCCService subject;


    @BeforeEach
    void setUp() {
        unblockedRepository = mock(UnblockedRepository.class);
        blockedRepository = mock(BlockedRepository.class);

        subject = new MeteringCCService(unblockedRepository,
                blockedRepository,
                clock);
    }

    MeteringCCRequest meteringCCRequest = MeteringCCRequest.builder()
            .carId("CAR1234")
            .requestId("cust119")
            .build();

    @Test
    void getBlockList_allowsAccess_whenResultNull() {

        List<MeteringCCBlockList> hasAccess = subject.getBlockList(meteringCCRequest);
        assertThat(hasAccess).isEqualTo(null);
    }

    @Test
    void getBlockList_allowsAccess_whenResultOne() {
        // db 조회내용 set
        Blocked blocked = new Blocked();
        List<Blocked> list = new ArrayList<Blocked>();

        blocked.setHandPhoneId("CAR123");
        blocked.setCarId("CAR123");
        blocked.setBlockedRsonCd("1004");
        blocked.setBlockedTime(OffsetDateTime.now());

        list.add(blocked);

        when(blockedRepository.findAllByCarId("CAR1234"))
                .thenReturn(list);

        // 결과 data set
        MeteringCCBlockList meteringCCBlockList = new MeteringCCBlockList();
        meteringCCBlockList.setCcid("CAR123");
        meteringCCBlockList.setRsonCd("10분 한도초과");
        meteringCCBlockList.setBlockedDate(OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyMMddHHmmss")));

        List<MeteringCCBlockList> Resultlist = new ArrayList<MeteringCCBlockList>();

        Resultlist.add(meteringCCBlockList);

        // 조회
        List<MeteringCCBlockList> hasAccess = subject.getBlockList(meteringCCRequest);
        assertThat(hasAccess).isEqualTo(Resultlist);
    }

    @Test
    void getBlockList_allowsAccess_whenResultList() {
        // db 조회내용 set
        Blocked blocked = new Blocked();
        List<Blocked> list = new ArrayList<Blocked>();

        blocked.setHandPhoneId("CAR123");
        blocked.setCarId("CAR123");
        blocked.setBlockedRsonCd("1004");
        blocked.setBlockedTime(OffsetDateTime.now());

        list.add(blocked);
        blocked = new Blocked();

        blocked.setHandPhoneId("CAR5432");
        blocked.setCarId("CAR123");
        blocked.setBlockedRsonCd("1005");
        blocked.setBlockedTime(OffsetDateTime.now());
        list.add(blocked);

        when(blockedRepository.findAllByCarId("CAR1234"))
                .thenReturn(list);

        // 결과 data set
        List<MeteringCCBlockList> Resultlist = new ArrayList<MeteringCCBlockList>();

        MeteringCCBlockList meteringCCBlockList = new MeteringCCBlockList();
        meteringCCBlockList.setCcid("CAR123");
        meteringCCBlockList.setRsonCd("10분 한도초과");
        meteringCCBlockList.setBlockedDate(OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyMMddHHmmss")));

        Resultlist.add(meteringCCBlockList);

        meteringCCBlockList = new MeteringCCBlockList();
        meteringCCBlockList.setCcid("CAR5432");
        meteringCCBlockList.setRsonCd("일 한도 초과");
        meteringCCBlockList.setBlockedDate(OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyMMddHHmmss")));

        Resultlist.add(meteringCCBlockList);
        // 조회
        List<MeteringCCBlockList> hasAccess = subject.getBlockList(meteringCCRequest);
        assertThat(hasAccess).isEqualTo(Resultlist);
    }


    @Test
    void unblock_allowsAccess_whenResultNull() {

        int hasAccess = subject.unblock(meteringCCRequest);
        assertThat(hasAccess).isEqualTo(0);

    }

    @Test
    void unblock_allowsAccess_whenDataSetOne() {

        // db 조회내용 set
        Blocked blocked = new Blocked();
        List<Blocked> list = new ArrayList<Blocked>();

        blocked.setHandPhoneId("CAR123");
        blocked.setCarId("CAR123");
        blocked.setBlockedRsonCd("1004");
        blocked.setBlockedTime(OffsetDateTime.now());

        list.add(blocked);

        when(blockedRepository.findAllByCarId("CAR1234"))
                .thenReturn(list);

        when(blockedRepository.deleteByCarId("CAR1234"))
                .thenReturn(1);

        int hasAccess = subject.unblock(meteringCCRequest);

        assertThat(hasAccess).isEqualTo(1);
    }

}