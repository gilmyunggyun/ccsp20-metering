package com.hkmc.ccs.metering.config;

import jakarta.servlet.DispatcherType;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }


  private static final String ENV_PATH = "/actuator/env";

  private static final String SERVICE_PATH = "/metering/**";


  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http.csrf().disable().cors().disable()
        .authorizeHttpRequests(request-> request
            .requestMatchers(SERVICE_PATH, "/login", ENV_PATH).permitAll()
            .anyRequest().authenticated())
//        .authorizeHttpRequests(request-> request.anyRequest().authenticated())

        //.requestMatchers(SERVICE_PATH, "/login", ENV_PATH).permitAll()
//        .anyRequest().authenticated()
        .httpBasic(Customizer.withDefaults());


//  request->request
//            .requestMatchers(SERVICE_PATH, "/login", ENV_PATH).permitAll()
//            .anyRequest().authenticated()
//        ).httpBasic(Customizer.withDefaults());

//        .httpBasic().and()
//            .authorizeHttpRequests().anyRequest().permitAll();


    return http.build();
  }

//  @Bean
//  public WebSecurityCustomizer webSecurityCustomizer() {
//    // antMatchers 부분도 deprecated 되어 requestMatchers로 대체
////    return (web) -> web.ignoring().requestMatchers(ENV_PATH, SERVICE_PATH, "/login");
//  }

//  @Configuration
//  public static class ApplicationConfiguration {
//
//
//    protected void configure(HttpSecurity http) throws Exception {
//      http..antMatcher(SERVICE_PATH).authorizeRequests()
//        .anyRequest().permitAll()
//        .and()
//        .httpBasic().disable()
//        .csrf().disable();
//    }
//
//  }

//  @Configuration
//  @Profile("prd")
//  @Order(1)
//  public static class ActuatorConfiguration extends WebSecurityConfigurerAdapter {
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//      http
//        .antMatcher(ENV_PATH).authorizeRequests()
//        .anyRequest().authenticated()
//        .and()
//        .httpBasic()
//        .and()
//        .csrf().disable();
//    }
//
//  }

}
