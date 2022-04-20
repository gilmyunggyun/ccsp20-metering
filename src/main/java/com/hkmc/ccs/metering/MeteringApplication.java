package com.hkmc.ccs.metering;

import java.time.Clock;

import com.hkmc.annotation.ConnectedCarApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableAsync
@ConnectedCarApplication
public class MeteringApplication {

  public static void main(String[] args) {
    SpringApplication.run(MeteringApplication.class, args);
  }

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }

}
