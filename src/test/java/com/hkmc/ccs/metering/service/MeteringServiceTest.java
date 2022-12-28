package com.hkmc.ccs.metering.service;

import com.hkmc.ccs.metering.models.entity.ApiAccess;
import com.hkmc.ccs.metering.models.entity.Blocked;
import com.hkmc.ccs.metering.models.entity.BlockedId;
import com.hkmc.ccs.metering.models.entity.WarningApi;
import com.hkmc.ccs.metering.models.vo.MeteringCheckRequest;
import com.hkmc.ccs.metering.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RefreshScope
class MeteringServiceTest {

    private BlockedRepository blockedRepository;
    private BlockedTempRepository blockedTempRepository;
    private ApiAccessRepository apiAccessRepository;
    private AllowedApiRepository allowedApiRepository;
    private WarningApiRepository warningApiRepository;
    private BlockCountApiRepository blockCountApiRepository;
    private Clock clock;

    private MeteringService subject;

    private final int ALLOW_BLOCK = 1;
    private final int DATA_NOT_VALID = 2;

    @BeforeEach
    void setUp() {
        blockedRepository = mock(BlockedRepository.class);
        blockedTempRepository = mock(BlockedTempRepository.class);
        apiAccessRepository = mock(ApiAccessRepository.class);
        allowedApiRepository = mock(AllowedApiRepository.class);
        warningApiRepository = mock(WarningApiRepository.class);
        blockCountApiRepository = mock(BlockCountApiRepository.class);
        clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

        subject = new MeteringService(blockedRepository,
                blockedTempRepository,
                apiAccessRepository,
                allowedApiRepository,
                clock,
                warningApiRepository,
                blockCountApiRepository);
    }

    MeteringCheckRequest meteringCheckRequest = MeteringCheckRequest.builder()
            .serviceNo("V1")
            .carId("CAR1234")
            .hpId("HP1234")
            .reqUrl("/was1/tmc/ccsp/window.do")
            .build();

    @Test
    void checkAccess_allowsAccess() {
        assertThat(subject.checkAccess(meteringCheckRequest,"testxtid")).isEqualTo(0);
    }

    @Test
    void checkAccess_addApiAccessRecords() {
        subject.checkAccess(meteringCheckRequest,"testxtid");

        verify(apiAccessRepository).save(ArgumentMatchers.eq(ApiAccess.builder()
                .handPhoneId("HP1234")
                .carId("CAR1234")
                .requestUrl("/window.do")
                .accessTime(OffsetDateTime.now(clock))
                .build()));
    }

    @Test
    void checkAccess_withBlockedCustomer_deniesAccess() {
        String[] remoteControlWhiteList = {"/pushhistorylist.do","/getbadgecount.do","/readmsg.do"};
        ReflectionTestUtils.setField(subject,"remoteControlWhiteList",remoteControlWhiteList);
        ReflectionTestUtils.setField(subject,"ALLOW_ACCESS",0);
        ReflectionTestUtils.setField(subject,"ALLOW_BLOCK",1);
        ReflectionTestUtils.setField(subject,"DATA_NOT_VALID",2);

        //Arrange
        when(blockedRepository.findById(any()))
                .thenReturn(Optional.of(Blocked.builder().build()));
        Blocked chkdata = new Blocked("HP1234", "CAR1234", "1004", OffsetDateTime.now(clock));

        when(blockedRepository.findById(BlockedId.builder()
                .handPhoneId("HP1234")
                .carId("CAR1234")
                .build())).thenReturn(Optional.of(chkdata));

        //Action
        int hasAccess = subject.checkAccess(meteringCheckRequest,"testxtid");

        //Assert
        assertThat(hasAccess).isEqualTo(ALLOW_BLOCK);

        verify(blockedRepository).findById(eq(BlockedId.builder()
                .handPhoneId("HP1234")
                .carId("CAR1234")
                .build()));
    }

    @Test
    void checkAccess_withBlockedCustomer_doesntAddApiRecords() {
        String[] remoteControlWhiteList = {"/pushhistorylist.do","/getbadgecount.do","/readmsg.do"};
        ReflectionTestUtils.setField(subject,"remoteControlWhiteList",remoteControlWhiteList);
        ReflectionTestUtils.setField(subject,"ALLOW_ACCESS",0);
        ReflectionTestUtils.setField(subject,"ALLOW_BLOCK",1);
        ReflectionTestUtils.setField(subject,"DATA_NOT_VALID",2);

        //Arrange
        when(blockedRepository.findById(any()))
                .thenReturn(Optional.of(Blocked.builder().build()));

        //Action
        int hasAccess = subject.checkAccess(meteringCheckRequest,"testxtid");

        //Assert
        assertThat(hasAccess).isEqualTo(ALLOW_BLOCK);

        verifyNoInteractions(apiAccessRepository);
    }

    @Test
    void checkAccess_with199RequestsInLast10Mins_allowsAccess() {
        String[] remoteControlWhiteList = {"/pushhistorylist.do","/getbadgecount.do","/readmsg.do"};
        ReflectionTestUtils.setField(subject,"remoteControlWhiteList",remoteControlWhiteList);
        ReflectionTestUtils.setField(subject,"ALLOW_ACCESS",0);
        ReflectionTestUtils.setField(subject,"ALLOW_BLOCK",1);
        ReflectionTestUtils.setField(subject,"DATA_NOT_VALID",2);

        when(apiAccessRepository.lastTimeAccessCount(
                "HP1234",
                "CAR1234",
                "/was1/tmc/ccsp/window.do",
                OffsetDateTime.now(clock).minusMinutes(10)
        )).thenReturn(199L);

        int hasAccess = subject.checkAccess(meteringCheckRequest,"testxtid");

        assertThat(hasAccess).isEqualTo(0);
    }

    @Test
    void checkAccess_with200RequestsInLast10Mins_deniesAccess() {
        String[] remoteControlWhiteList = {"/pushhistorylist.do","/getbadgecount.do","/readmsg.do"};
        ReflectionTestUtils.setField(subject,"remoteControlWhiteList",remoteControlWhiteList);
        ReflectionTestUtils.setField(subject,"ALLOW_ACCESS",0);
        ReflectionTestUtils.setField(subject,"ALLOW_BLOCK",1);
        ReflectionTestUtils.setField(subject,"DATA_NOT_VALID",2);

        when(apiAccessRepository.lastTimeAccessCount(
                "HP1234",
                "CAR1234",
                "/window.do",
                OffsetDateTime.now(clock).minusMinutes(10)
        )).thenReturn(200L);

        when(blockCountApiRepository.countByRequestUrl("/window.do")).thenReturn(1L);

        int hasAccess = subject.checkAccess(meteringCheckRequest,"testxtid");

        assertThat(hasAccess).isEqualTo(ALLOW_BLOCK);
    }

    @Test
    void checkAccess_with200RequestsInLast10Mins_blocksCustomer() {
        String[] remoteControlWhiteList = {"/pushhistorylist.do","/getbadgecount.do","/readmsg.do"};
        ReflectionTestUtils.setField(subject,"remoteControlWhiteList",remoteControlWhiteList);
        ReflectionTestUtils.setField(subject,"ALLOW_ACCESS",0);
        ReflectionTestUtils.setField(subject,"ALLOW_BLOCK",1);
        ReflectionTestUtils.setField(subject,"DATA_NOT_VALID",2);

        when(apiAccessRepository.lastTimeAccessCount(
                "HP1234",
                "CAR1234",
                "/window.do",
                OffsetDateTime.now(clock).minusMinutes(10)
        )).thenReturn(200L);

        when(blockCountApiRepository.countByRequestUrl("/window.do")).thenReturn(1L);

        subject.checkAccess(meteringCheckRequest,"testxtid");

        verify(blockedRepository).save(Blocked.builder()
                .handPhoneId("HP1234")
                .carId("CAR1234")
                .blockedTime(OffsetDateTime.now(clock))
                .blockedRsonCd("1004")
                .build());
    }

    @Test
    void checkAccess_with299RequestsToday_allowsAccess() {
        String[] remoteControlWhiteList = {"/pushhistorylist.do","/getbadgecount.do","/readmsg.do"};
        ReflectionTestUtils.setField(subject,"remoteControlWhiteList",remoteControlWhiteList);
        ReflectionTestUtils.setField(subject,"ALLOW_ACCESS",0);
        ReflectionTestUtils.setField(subject,"ALLOW_BLOCK",1);
        ReflectionTestUtils.setField(subject,"DATA_NOT_VALID",2);

        when(apiAccessRepository.dailyAccessCount(
                "HP1234",
                "CAR1234",
                "/was1/tmc/ccsp/window.do"
        )).thenReturn(299L);

        int hasAccess = subject.checkAccess(meteringCheckRequest,"testxtid");

        assertThat(hasAccess).isEqualTo(0);
    }

    @Test
    void checkAccess_with301RequestsToday_deniesAccess() {
        String[] remoteControlWhiteList = {"/pushhistorylist.do","/getbadgecount.do","/readmsg.do"};
        ReflectionTestUtils.setField(subject,"remoteControlWhiteList",remoteControlWhiteList);
        ReflectionTestUtils.setField(subject,"ALLOW_ACCESS",0);
        ReflectionTestUtils.setField(subject,"ALLOW_BLOCK",1);
        ReflectionTestUtils.setField(subject,"DATA_NOT_VALID",2);

        when(apiAccessRepository.dailyAccessCount(
                "HP1234",
                "CAR1234",
                "/window.do"
        )).thenReturn(301L);

        when(blockCountApiRepository.countByRequestUrl("/window.do")).thenReturn(1L);

        int hasAccess = subject.checkAccess(meteringCheckRequest,"testxtid");

        assertThat(hasAccess).isEqualTo(ALLOW_BLOCK);
    }

    @Test
    void checkAccess_with300RequestsToday_blocksCustomer() {
        String[] remoteControlWhiteList = {"/pushhistorylist.do","/getbadgecount.do","/readmsg.do"};
        ReflectionTestUtils.setField(subject,"remoteControlWhiteList",remoteControlWhiteList);
        ReflectionTestUtils.setField(subject,"ALLOW_ACCESS",0);
        ReflectionTestUtils.setField(subject,"ALLOW_BLOCK",1);
        ReflectionTestUtils.setField(subject,"DATA_NOT_VALID",2);

        when(apiAccessRepository.dailyAccessCount(
                "HP1234",
                "CAR1234",
                "/window.do"
        )).thenReturn(300L);

        when(blockCountApiRepository.countByRequestUrl("/window.do")).thenReturn(1L);

        subject.checkAccess(meteringCheckRequest,"testxtid");

        verify(blockedRepository).save(Blocked.builder()
                .handPhoneId("HP1234")
                .carId("CAR1234")
                .blockedTime(OffsetDateTime.now(clock))
                .blockedRsonCd("1005")
                .build());
    }

    @Test
    void checkAccess_withRequestUrlInExceptionList_allowsAccess_withoutCheckingOrRecording() {
        String[] remoteControlWhiteList = {"/pushhistorylist.do","/getbadgecount.do","/readmsg.do"};
        ReflectionTestUtils.setField(subject,"remoteControlWhiteList",remoteControlWhiteList);
        ReflectionTestUtils.setField(subject,"ALLOW_ACCESS",0);
        ReflectionTestUtils.setField(subject,"ALLOW_BLOCK",1);
        ReflectionTestUtils.setField(subject,"DATA_NOT_VALID",2);

        when(allowedApiRepository.countByRequestUrl("/pushVersion.do")).thenReturn(1L);

        int hasAccess = subject.checkAccess(MeteringCheckRequest.builder()
                .serviceNo("V1")
                .carId("CAR1234")
                .hpId("HP1234")
                .reqUrl("/pushVersion.do")
                .build(),"testxtid");

        assertThat(hasAccess).isEqualTo(0);
        verify(blockedRepository, atLeastOnce()).findById(any());
        verifyNoInteractions(apiAccessRepository);
        verify(blockedRepository, never()).save(any());
    }

    @Test
    void checkAccess_allowsAccess_withoutCarIdCase() {
        String[] remoteControlWhiteList = {"/pushhistorylist.do","/getbadgecount.do","/readmsg.do"};
        ReflectionTestUtils.setField(subject,"remoteControlWhiteList",remoteControlWhiteList);
        ReflectionTestUtils.setField(subject,"ALLOW_ACCESS",0);
        ReflectionTestUtils.setField(subject,"ALLOW_BLOCK",1);
        ReflectionTestUtils.setField(subject,"DATA_NOT_VALID",2);

        when(allowedApiRepository.countByRequestUrl(
                "/pushhistorylist.do"
        )).thenReturn(1L);

        int hasAccess = subject.checkAccess(MeteringCheckRequest.builder()
                .serviceNo("V1")
                .carId("")
                .hpId("HP1234")
                .reqUrl("/ccsp/pushhistorylist.do")
                .build(),"testxtid");


        assertThat(hasAccess).isEqualTo(0);
    }

    @Test
    void checkAccess_WarningApi() {
        String[] remoteControlWhiteList = {"/pushhistorylist.do","/getbadgecount.do","/readmsg.do"};
        ReflectionTestUtils.setField(subject,"remoteControlWhiteList",remoteControlWhiteList);
        ReflectionTestUtils.setField(subject,"ALLOW_ACCESS",0);
        ReflectionTestUtils.setField(subject,"ALLOW_BLOCK",1);
        ReflectionTestUtils.setField(subject,"DATA_NOT_VALID",2);

        when(apiAccessRepository.dailyAccessCount(
                "HP1234",
                "CAR1234",
                "/window.do"
        )).thenReturn(300L);

        when(warningApiRepository.countByRequestUrl("/window.do")).thenReturn(1L);

        int hasAccess = subject.checkAccess(meteringCheckRequest,"testxtid");

        verify(warningApiRepository, times(0)).save(WarningApi.builder()
                .requestUrl("/window.do")
                .build());

        assertThat(hasAccess).isEqualTo(0);

    }

    @Test
    void checkAccess_newWarningApi() {
        String[] remoteControlWhiteList = {"/pushhistorylist.do","/getbadgecount.do","/readmsg.do"};
        ReflectionTestUtils.setField(subject,"remoteControlWhiteList",remoteControlWhiteList);
        ReflectionTestUtils.setField(subject,"ALLOW_ACCESS",0);
        ReflectionTestUtils.setField(subject,"ALLOW_BLOCK",1);
        ReflectionTestUtils.setField(subject,"DATA_NOT_VALID",2);

        when(apiAccessRepository.dailyAccessCount(
                "HP1234",
                "CAR1234",
                "/window.do"
        )).thenReturn(300L);

        when(warningApiRepository.countByRequestUrl("/window.do")).thenReturn(0L);

        int hasAccess = subject.checkAccess(meteringCheckRequest,"testxtid");

        verify(warningApiRepository).save(WarningApi.builder()
                        .requestUrl("/window.do")
                        .build());

        assertThat(hasAccess).isEqualTo(0);
    }
}