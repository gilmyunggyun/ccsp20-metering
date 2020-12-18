package com.hyundaiautoever.ccs.metering;

import ccs.core.data.encrypt.EnablePropertyEncrypt;
import com.hyundaiautoever.transactionlogger.TransactionLoggerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Clock;

@EnableAsync
@SpringBootApplication
@Import(TransactionLoggerConfiguration.class)
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
