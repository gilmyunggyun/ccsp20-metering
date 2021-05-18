package com.hkmc.ccs.metering.service;

import com.hkmc.ccs.metering.models.entity.ApiAccess;
import com.hkmc.ccs.metering.models.entity.Blocked;
import com.hkmc.ccs.metering.models.entity.BlockedId;
import com.hkmc.ccs.metering.models.vo.MeteringCheckRequest;
import com.hkmc.ccs.metering.repository.AllowedApiRepository;
import com.hkmc.ccs.metering.repository.ApiAccessRepository;
import com.hkmc.ccs.metering.repository.BlockedRepository;
import com.hkmc.ccs.metering.repository.BlockedTempRepository;
import com.hkmc.ccs.metering.service.MeteringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.cloud.context.config.annotation.RefreshScope;

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
    private Clock clock;

    private MeteringService subject;

    @BeforeEach
    void setUp() {
        blockedRepository = mock(BlockedRepository.class);
        blockedTempRepository = mock(BlockedTempRepository.class);
        apiAccessRepository = mock(ApiAccessRepository.class);
        allowedApiRepository = mock(AllowedApiRepository.class);
        clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

        subject = new MeteringService(blockedRepository,
                blockedTempRepository,
                apiAccessRepository,
                allowedApiRepository,
                clock);
    }

    MeteringCheckRequest meteringCheckRequest = MeteringCheckRequest.builder()
            .serviceNo("V1")
            .carId("CAR1234")
            .hpId("HP1234")
            .reqUrl("/was1/tmc/ccsp/window.do")
            .build();

    @Test
    void checkAccess_allowsAccess() {
        assertThat(subject.checkAccess(meteringCheckRequest,"testxtid")).isTrue();
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

        //Arrange
        when(blockedRepository.findById(any()))
                .thenReturn(Optional.of(Blocked.builder().build()));
        Blocked chkdata = new Blocked("HP1234", "CAR1234", "1004", OffsetDateTime.now(clock));

        when(blockedRepository.findById(BlockedId.builder()
                .handPhoneId("HP1234")
                .carId("CAR1234")
                .build())).thenReturn(Optional.of(chkdata));

        //Action
        boolean hasAccess = subject.checkAccess(meteringCheckRequest,"testxtid");

        //Assert
        assertThat(hasAccess).isFalse();

        verify(blockedRepository).findById(eq(BlockedId.builder()
                .handPhoneId("HP1234")
                .carId("CAR1234")
                .build()));
    }

    @Test
    void checkAccess_withBlockedCustomer_doesntAddApiRecords() {

        //Arrange
        when(blockedRepository.findById(any()))
                .thenReturn(Optional.of(Blocked.builder().build()));

        //Action
        boolean hasAccess = subject.checkAccess(meteringCheckRequest,"testxtid");

        //Assert
        assertThat(hasAccess).isFalse();

        verifyNoInteractions(apiAccessRepository);
    }

    @Test
    void checkAccess_with199RequestsInLast10Mins_allowsAccess() {
        when(apiAccessRepository.countByHandPhoneIdAndCarIdAndRequestUrlAndAccessTimeAfter(
                "HP1234",
                "CAR1234",
                "/was1/tmc/ccsp/window.do",
                OffsetDateTime.now(clock).minusMinutes(10)
        )).thenReturn(199L);

        boolean hasAccess = subject.checkAccess(meteringCheckRequest,"testxtid");

        assertThat(hasAccess).isTrue();
    }

    @Test
    void checkAccess_with200RequestsInLast10Mins_deniesAccess() {
        when(apiAccessRepository.countByHandPhoneIdAndCarIdAndRequestUrlAndAccessTimeAfter(
                "HP1234",
                "CAR1234",
                "/window.do",
                OffsetDateTime.now(clock).minusMinutes(10)
        )).thenReturn(200L);

        boolean hasAccess = subject.checkAccess(meteringCheckRequest,"testxtid");

        assertThat(hasAccess).isFalse();
    }

    @Test
    void checkAccess_with200RequestsInLast10Mins_blocksCustomer() {
        when(apiAccessRepository.countByHandPhoneIdAndCarIdAndRequestUrlAndAccessTimeAfter(
                "HP1234",
                "CAR1234",
                "/window.do",
                OffsetDateTime.now(clock).minusMinutes(10)
        )).thenReturn(200L);

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
        when(apiAccessRepository.dailyAccessCount(
                "HP1234",
                "CAR1234",
                "/was1/tmc/ccsp/window.do"
        )).thenReturn(299L);

        boolean hasAccess = subject.checkAccess(meteringCheckRequest,"testxtid");

        assertThat(hasAccess).isTrue();
    }

    @Test
    void checkAccess_with301RequestsToday_deniesAccess() {
        when(apiAccessRepository.dailyAccessCount(
                "HP1234",
                "CAR1234",
                "/window.do"
        )).thenReturn(301L);

        boolean hasAccess = subject.checkAccess(meteringCheckRequest,"testxtid");

        assertThat(hasAccess).isFalse();
    }

    @Test
    void checkAccess_with300RequestsToday_blocksCustomer() {
        when(apiAccessRepository.dailyAccessCount(
                "HP1234",
                "CAR1234",
                "/window.do"
        )).thenReturn(300L);

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
        when(allowedApiRepository.countByRequestUrl("/pushVersion.do")).thenReturn(1L);

        boolean hasAccess = subject.checkAccess(MeteringCheckRequest.builder()
                .serviceNo("V1")
                .carId("CAR1234")
                .hpId("HP1234")
                .reqUrl("/pushVersion.do")
                .build(),"testxtid");

        assertThat(hasAccess).isTrue();
        verify(blockedRepository, atLeastOnce()).findById(any());
        verifyNoInteractions(apiAccessRepository);
        verify(blockedRepository, never()).save(any());
    }
}