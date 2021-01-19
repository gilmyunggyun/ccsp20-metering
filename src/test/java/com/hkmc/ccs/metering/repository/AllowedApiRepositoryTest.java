package com.hkmc.ccs.metering.repository;

import com.hkmc.ccs.metering.models.entity.AllowedApi;
import com.hkmc.ccs.metering.repository.AllowedApiRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;

@DataJpaTest
class AllowedApiRepositoryTest {

    @Autowired
    private AllowedApiRepository subject;

    @Test
    void countByRequestUrl_countsByRequestUrl() {
        subject.saveAll(list(
                AllowedApi.builder().requestUrl("same").build(),
                AllowedApi.builder().requestUrl("same").build(),
                AllowedApi.builder().requestUrl("different").build()
        ));

        long count = subject.countByRequestUrl("same");

        assertThat(count).isEqualTo(2L);
    }
}