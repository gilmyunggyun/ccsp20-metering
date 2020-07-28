package com.hyundaiautoever.ccs.metering;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class RequestResponseLoggerFilterTestController {
    @PostMapping(value = "/test")
    public JsonNode postTest(@RequestBody String body) throws JsonProcessingException {
        System.out.println("using body in test controller... " + body);
        return new JsonMapper().readTree("{\"foo\": \"response\"}");
    }

    @GetMapping("/test")
    public JsonNode getTest() throws JsonProcessingException {
        return new JsonMapper().readTree("{\"foo\": \"response\"}");
    }

    @PostMapping("/test/error")
    public void postErrorTest() {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
