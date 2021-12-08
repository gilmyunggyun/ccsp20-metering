package com.hkmc.ccs.metering;

import ccs.core.data.encrypt.EnablePropertyEncrypt;
import com.hkmc.transactionlogger.EnableCcsp20TransactionLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Clock;

@EnableAsync
@SpringBootApplication
//@EnableCcsp20TransactionLogger
@EnablePropertyEncrypt
public class MeteringApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeteringApplication.class, args);
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
