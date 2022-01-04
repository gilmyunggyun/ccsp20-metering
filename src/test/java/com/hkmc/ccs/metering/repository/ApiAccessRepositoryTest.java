package com.hkmc.ccs.metering.repository;

import com.hkmc.ccs.metering.models.entity.ApiAccess;
import com.hkmc.ccs.metering.repository.ApiAccessRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("local")
class ApiAccessRepositoryTest {

    @Autowired
    private ApiAccessRepository subject;

    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    private final OffsetDateTime now = OffsetDateTime.now(clock);
    private final OffsetDateTime nineMinutesAgo = OffsetDateTime.now(clock).minusMinutes(9L);
    private final OffsetDateTime tenMinutesAgo = OffsetDateTime.now(clock).minusMinutes(11L);
    private final OffsetDateTime yesterday = OffsetDateTime.now(clock).minusDays(1L);

    @Test
    void count_countsOnlyRecordsAfterPassedInTime() {
        // Arrange
        subject.saveAll(list(
                apiAccessRecord(now),
                apiAccessRecord(nineMinutesAgo),
                apiAccessRecord(tenMinutesAgo)
        ));

        // Act
        long count = subject.lastTimeAccessCount(
                "HP1234",
                "CAR1234",
                "/was1/tmc/ccsp/window.do",
                tenMinutesAgo
        );

        // Assert
        assertThat(count).isEqualTo(3L);
    }

    @Test
    void count_countsOnlyRecordsWithCorrectIds() {
        subject.save(apiAccessRecord(now));
        subject.save(ApiAccess.builder()
                .handPhoneId("OTHERHP")
                .carId("OTHERCAR")
                .requestUrl("/was1/tmc/ccsp/window.do")
                .accessTime(now)
                .build());

        long count = subject.lastTimeAccessCount(
                "HP1234",
                "CAR1234",
                "/was1/tmc/ccsp/window.do",
                tenMinutesAgo
        );

        assertThat(count).isEqualTo(1L);
    }

    @Test
    void dailyAccessCount_countsRecordsToday() {
        subject.save(apiAccessRecord(now));
        subject.save(apiAccessRecord(yesterday));

        long count = subject.dailyAccessCount(
                "HP1234",
                "CAR1234",
                "/was1/tmc/ccsp/window.do"
        );

        assertThat(count).isEqualTo(1L);
    }

    private ApiAccess apiAccessRecord(OffsetDateTime accessTime) {
        return ApiAccess.builder().handPhoneId("HP1234").carId("CAR1234").requestUrl("/was1/tmc/ccsp/window.do").accessTime(accessTime).build();
    }
}