package com.hkmc.ccs.metering;

//import ccs.core.data.encrypt.EnablePropertyEncrypt;
//import com.hkmc.annotation.ConnectedCarApplication;
//import com.hkmc.filter.EnableTransactionLogger;
import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableAsync
@SpringBootApplication
//@EnablePropertyEncrypt
//@EnableTransactionLogger
public class MeteringApplication {

  public static void main(String[] args) {
    SpringApplication.run(MeteringApplication.class, args);
  }


}
