package com.hyundaiautoever.ccs.metering;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class MeteringApplicationTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    @Test
    void contextLoads() {
    }

    @Test
    void happyPath() throws JSONException {
        HttpHeaders headers = new HttpHeaders();
        headers.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);

        RequestEntity<String> request = new RequestEntity<>(
                "{\"serviceNo\": \"V1\", \"hpId\": \"HP1234\", \"carId\": \"CAR1234\", \"reqUrl\": \"/was1/tmc/ccsp/window.do\"}",
                headers,
                HttpMethod.POST,
                URI.create("http://localhost:" + port + "/metering/v1/metering")
        );

        ResponseEntity<String> response = testRestTemplate.exchange(request, String.class);

        assertEquals(
                "{\"ServiceNo\": \"V1\", \"RetCode\": \"S\", \"resCode\": \"0000\"}",
                response.getBody(),
                JSONCompareMode.NON_EXTENSIBLE
        );
    }
}