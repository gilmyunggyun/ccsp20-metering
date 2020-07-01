package com.hyundaiautoever.ccs.metering;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeteringServiceTest {

    @Mock
    private BlockedRepository blockedRepository;

    @Mock
    private ApiAccessRepository apiAccessRepository;

    @InjectMocks
    private MeteringService subject;

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