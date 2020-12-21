package com.hyundaiautoever.ccs.metering;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final Environment env;

    @Autowired
    public SecurityConfig(Environment env) {
        this.env = env;
    }

    private static String PRD = "prd";
    private static String ENV_PATH = "/actuator/env";

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (StringUtils.equalsAny(PRD, env.getActiveProfiles())) {
            http.authorizeRequests()
                    .antMatchers(ENV_PATH).authenticated()
                    .anyRequest().permitAll()
                    .and()
                    .httpBasic().disable()
                    .csrf().disable();
        } else {
            http.authorizeRequests()
                    .anyRequest().permitAll()
                    .and()
                    .httpBasic().disable()
                    .csrf().disable();

        }
    }
}