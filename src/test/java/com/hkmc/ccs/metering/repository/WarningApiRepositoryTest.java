package com.hkmc.ccs.metering.repository;

import com.hkmc.ccs.metering.models.entity.WarningApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;

@DataJpaTest
@ActiveProfiles("local")
class WarningApiRepositoryTest {

    @Autowired
    private WarningApiRepository subject;

    @Test
    void countByRequestUrl_countsByRequestUrl() {
        subject.saveAll(list(
                WarningApi.builder().requestUrl("same").build(),
                WarningApi.builder().requestUrl("same").build(),
                WarningApi.builder().requestUrl("different").build()
        ));

        long count = subject.countByRequestUrl("same");

        assertThat(count).isEqualTo(2L);
    }
}