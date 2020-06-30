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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeteringServiceTest {

    @Mock
    private BlockedRepository blockedRepository;

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
}