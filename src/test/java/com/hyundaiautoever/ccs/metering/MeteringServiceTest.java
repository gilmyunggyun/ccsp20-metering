package com.hyundaiautoever.ccs.metering;

import com.hyundaiautoever.ccs.metering.MeteringService.AccessCheckResult;
import com.hyundaiautoever.ccs.metering.VO.MeteringCheckRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MeteringServiceTest {

    private BlockedRepository blockedRepository;
    private ApiAccessRepository apiAccessRepository;
    private AllowedApiRepository allowedApiRepository;
    private Clock clock;

    private MeteringService subject;

    @BeforeEach
    void setUp() {
        blockedRepository = mock(BlockedRepository.class);
        apiAccessRepository = mock(ApiAccessRepository.class);
        allowedApiRepository = mock(AllowedApiRepository.class);
        clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

        subject = new MeteringService(blockedRepository,
                apiAccessRepository,
                allowedApiRepository,
                clock);
    }

    @Value("${metering.BLOCK_BY_API}")
    private String BLOCK_BY_API;

    @Value("${metering.SERVICE_SUCCESS}")
    private String SERVICE_SUCCESS;

    @Value("${metering.MSG_FORMAT_INVALID}")
    private String MSG_FORMAT_INVALID;

    MeteringCheckRequest meteringCheckRequest = MeteringCheckRequest.builder()
            .serviceNo("V1")
            .carId("CAR1234")
            .hpId("HP1234")
            .reqUrl("/ccsp/window.do")
            .build();

    AccessCheckResult success = AccessCheckResult.builder()
            .RetCode("S")
            .resCode("0000")
            .build();

    AccessCheckResult blocked = AccessCheckResult.builder()
            .RetCode("B")
            .resCode("BK02")
            .build();

    @Test
    void checkAccess_allowsAccess() {
        assertThat(subject.checkAccess(meteringCheckRequest)).isEqualTo(success);
    }


    @Test
    void checkAccess_addApiAccessRecords() {
        subject.checkAccess(meteringCheckRequest);

        verify(apiAccessRepository).save(eq(ApiAccess.builder()
                .handPhoneId("HP1234")
                .carId("CAR1234")
                .requestUrl("/ccsp/window.do")
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
        AccessCheckResult hasAccess = subject.checkAccess(meteringCheckRequest);

        //Assert
        assertThat(hasAccess).isEqualTo(blocked);

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
        AccessCheckResult hasAccess = subject.checkAccess(meteringCheckRequest);

        //Assert
        assertThat(hasAccess).isEqualTo(blocked);

        verifyNoInteractions(apiAccessRepository);
    }

    @Test
    void checkAccess_with199RequestsInLast10Mins_allowsAccess() {
        when(apiAccessRepository.countByHandPhoneIdAndCarIdAndRequestUrlAndAccessTimeAfter(
                "HP1234",
                "CAR1234",
                "/ccsp/window.do",
                OffsetDateTime.now(clock).minusMinutes(10)
        )).thenReturn(199L);

        AccessCheckResult hasAccess = subject.checkAccess(meteringCheckRequest);

        assertThat(hasAccess).isEqualTo(success);
    }

    @Test
    void checkAccess_with200RequestsInLast10Mins_deniesAccess() {
        //check except metering service and if cnt overs, insert isol table
        // data sync with legacy
        // max count can be changed ( declare constant?)
        when(apiAccessRepository.countByHandPhoneIdAndCarIdAndRequestUrlAndAccessTimeAfter(
                "HP1234",
                "CAR1234",
                "/ccsp/window.do",
                OffsetDateTime.now(clock).minusMinutes(10)
        )).thenReturn(200L);

        AccessCheckResult hasAccess = subject.checkAccess(meteringCheckRequest);

        assertThat(hasAccess).isEqualTo(blocked);
    }

    @Test
    void checkAccess_with200RequestsInLast10Mins_blocksCustomer() {
        when(apiAccessRepository.countByHandPhoneIdAndCarIdAndRequestUrlAndAccessTimeAfter(
                "HP1234",
                "CAR1234",
                "/ccsp/window.do",
                OffsetDateTime.now(clock).minusMinutes(10)
        )).thenReturn(200L);

        subject.checkAccess(meteringCheckRequest);

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
                "/ccsp/window.do"
        )).thenReturn(299L);

        AccessCheckResult hasAccess = subject.checkAccess(meteringCheckRequest);

        assertThat(hasAccess).isEqualTo(success);
    }

    @Test
    void checkAccess_with301RequestsToday_deniesAccess() {
        when(apiAccessRepository.dailyAccessCount(
                "HP1234",
                "CAR1234",
                "/ccsp/window.do"
        )).thenReturn(301L);

        AccessCheckResult hasAccess = subject.checkAccess(meteringCheckRequest);

        assertThat(hasAccess).isEqualTo(blocked);
    }

    @Test
    void checkAccess_with300RequestsToday_blocksCustomer() {
        when(apiAccessRepository.dailyAccessCount(
                "HP1234",
                "CAR1234",
                "/ccsp/window.do"
        )).thenReturn(300L);

        subject.checkAccess(meteringCheckRequest);

        verify(blockedRepository).save(Blocked.builder()
                .handPhoneId("HP1234")
                .carId("CAR1234")
                .blockedTime(OffsetDateTime.now(clock))
                .blockedRsonCd("1005")
                .build());
    }

    @Test
    void checkAccess_withRequestUrlInExceptionList_allowsAccess_withoutCheckingOrRecording() {
        when(allowedApiRepository.countByRequestUrl("/versionCheck.do")).thenReturn(1L);

        AccessCheckResult hasAccess = subject.checkAccess(MeteringCheckRequest.builder()
                .serviceNo("V1")
                .carId("CAR1234")
                .hpId("HP1234")
                .reqUrl("/versionCheck.do")
                .build());

        assertThat(hasAccess).isEqualTo(success);
        verify(blockedRepository, atLeastOnce()).findById(any());
        verifyNoInteractions(apiAccessRepository);
        verify(blockedRepository, never()).save(any());
    }
}