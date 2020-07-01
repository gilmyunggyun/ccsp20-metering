package com.hyundaiautoever.ccs.metering;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    private Clock clock;

    private MeteringService subject;

    @BeforeEach
    void setUp() {
        blockedRepository = mock(BlockedRepository.class);
        apiAccessRepository = mock(ApiAccessRepository.class);
        clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

        subject = new MeteringService(blockedRepository,
                apiAccessRepository,
                clock);
    }

    @Test
    void checkAccess_allowsAccess() {
        assertThat(subject.checkAccess(
                "V1",
                "HP1234",
                "CAR1234",
                "/ccsp/window.do")).isTrue();
    }

    @Test
    void checkAccess_addApiAccessRecords() {
        subject.checkAccess(
                "V1",
                "HP1234",
                "CAR1234",
                "/ccsp/window.do");

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

        //Action
        boolean hasAccess = subject.checkAccess("V1", "HP1234", "CAR1234", "/ccsp/window.do");

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
        boolean hasAccess = subject.checkAccess("V1", "HP1234", "CAR1234", "/ccsp/window.do");

        //Assert
        assertThat(hasAccess).isFalse();

        verifyNoInteractions(apiAccessRepository);

    }

    @Test
    void checkAccess_with199RequestsInLast10Mins_allowsAccess() {

    }

    @Test
    void checkAccess_with200RequestsInLast10Mins_deniesAccess() {
        //check except metering service and if cnt overs, insert isol table
            // data sync with legacy
        // max count can be changed ( declare constant?)
    }

    @Test
    void checkAccess_with299RequestsToday_allowsAccess() {

    }

    @Test
    void checkAccess_with300RequestsToday_deniesAccess() {
//if cnt overs, insert isol table
    }
}