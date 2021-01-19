package com.hkmc.ccs.metering.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Configuration
public class SecurityConfig {

    private static String ENV_PATH = "/actuator/env";

    private static String SERVICE_PATH = "/metering/**";

    @Configuration
    public static class ApplicationConfiguration extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher(SERVICE_PATH).authorizeRequests()
                    .anyRequest().permitAll()
                    .and()
                    .httpBasic().disable()
                    .csrf().disable();
        }

    }


    @Configuration
    @Profile("prd")
    @Order(1)
    public static class ActuatorConfiguration extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .antMatcher(ENV_PATH).authorizeRequests()
                    .anyRequest().authenticated()
                    .and()
                    .httpBasic()
                    .and()
                    .csrf().disable();
        }
    }
}