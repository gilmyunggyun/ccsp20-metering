package com.hyundaiautoever.ccs.metering;

import com.hyundaiautoever.ccs.metering.requestResponseLogging.RequestResponseLoggerFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {
        RequestResponseLoggerFilterTestController.class,
        RequestResponseLoggerFilter.class,
})
public class RequestResponseLoggerFilterTests {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("requestResponseLogger")
    Logger logger;


    @MockBean
    @Qualifier("controllerExceptionHandlerLogger")
    Logger controllerExceptionHandlerLogger;

    @BeforeEach
    void setUp() {
        reset(logger);
    }

    @Test
    void logsRequestsWithBody() throws Exception {
        String requestBody = "{\"foo\":\"bar\"}";

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("XTID".toLowerCase(), "TESTTID");
        requestHeaders.set(CONTENT_TYPE.toLowerCase(), APPLICATION_JSON_VALUE);
        requestHeaders.set(CONTENT_LENGTH.toLowerCase(), Integer.toString(requestBody.length()));
        requestHeaders.set(AUTHORIZATION.toLowerCase(), "testauth");

        mockMvc.perform(post("/test")
                .content(requestBody)
                .contentType(APPLICATION_JSON)
                .header("XTID", "TESTTID")
                .header(AUTHORIZATION, "testauth")
        ).andExpect(status().isOk());


        verify(logger, times(1))
                .info(eq("Logging Request {}:: Method : {}, URI : {}, Headers: {}, Body: {}"),
                        eq("TESTTID"),
                        eq("POST"),
                        eq("/test"),
                        eq(requestHeaders),
                        eq(requestBody));
    }

    @Test
    void logsRequestsWithoutBody() throws Exception {

        mockMvc.perform(get("/test")
                .header("XTID", "TESTTID")
                .header(AUTHORIZATION, "testauth")
        ).andExpect(status().isOk());;

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("XTID".toLowerCase(), "TESTTID");
        requestHeaders.add(AUTHORIZATION.toLowerCase(), "testauth");
        verify(logger, times(1))
                .info("Logging Request {}:: Method : {}, URI : {}, Headers: {}, Body: {}",
                        "TESTTID",
                        "GET",
                        "/test",
                        requestHeaders,
                        "");
    }

    @Test
    void doesNotLogResponseFor2XX() throws Exception {
        String requestBody = "{\"foo\": \"bar\"}";

        mockMvc.perform(post("/test")
                .content(requestBody)
                .contentType(APPLICATION_JSON)
                .header("XTID", "TESTTID")
                .header(AUTHORIZATION, "testauth")
        ).andExpect(status().isOk());;

        verify(logger, times(0))
                .info(eq("Logging Response {}:: Status : {}, Error : {}"),
                        anyString(),
                        anyInt(),
                        any());
    }

    @Test
    void logResponseForErrors() throws Exception {
        mockMvc.perform(post("/test/error")
                .header("XTID", "TESTTID")
        ).andExpect(status().isInternalServerError());;

        verify(logger, times(1))
                .info(eq("Logging Response {}:: Status : {}"),
                        eq("TESTTID"),
                        eq(500));
    }
}
