package com.hyundaiautoever.ccs.metering.requestResponseLogging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequestResponseLoggerConfiguration {
    Logger log = LoggerFactory.getLogger(RequestResponseLoggerFilter.class);
    @Bean(name = "requestResponseLogger")
    public Logger logger() {
        return log;
    }
}
