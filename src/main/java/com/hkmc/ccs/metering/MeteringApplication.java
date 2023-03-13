package com.hkmc.ccs.metering;

import ccs.core.data.encrypt.EnablePropertyEncrypt;
import com.hkmc.filter.EnableTransactionLogger;
import java.time.Clock;

import com.hkmc.annotation.ConnectedCarApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableAsync
@SpringBootApplication
@EnableFeignClients
@EnablePropertyEncrypt
@EnableTransactionLogger
public class MeteringApplication {

  public static void main(String[] args) {
    SpringApplication.run(MeteringApplication.class, args);
  }

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }

}
