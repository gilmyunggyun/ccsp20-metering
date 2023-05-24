package com.hkmc.ccs.metering;

//import ccs.core.data.encrypt.EnablePropertyEncrypt;
//import com.hkmc.annotation.ConnectedCarApplication;
//import com.hkmc.filter.EnableTransactionLogger;
import java.time.Clock;

import co.elastic.apm.attach.ElasticApmAttacher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableAsync
@SpringBootApplication
//@EnablePropertyEncrypt
//@EnableTransactionLogger
@EntityScan("com.hkmc.ccs.metering.models.entity")
public class MeteringApplication {

  public static void main(String[] args) {
    ElasticApmAttacher.attach();
    SpringApplication.run(MeteringApplication.class, args);
  }

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }

}
