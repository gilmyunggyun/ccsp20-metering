package com.hkmc.ccs.metering.controller;


import com.hkmc.ccs.metering.models.vo.MeteringCheckRequest;
import com.hkmc.ccs.metering.service.MeteringService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("local")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@WebMvcTest(value = {MeteringController.class})
public class MeteringControllerTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MeteringService meteringService;

    @MockBean
    @Qualifier("requestResponseLogger")
    Logger requestResponseLogger;

    MeteringCheckRequest meteringCheckRequest = MeteringCheckRequest.builder()
            .serviceNo("V1")
            .carId("CAR123456")
            .hpId("HP123456")
            .reqUrl("/ccsp/window.do")
            .build();

    @Test
    void checkAccess_whenControllerAllowsAccess_thenReturnSuccess() throws Exception {
        when(meteringService.checkAccess(any(),anyString())).thenReturn(0);

        makeRequest().andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"ServiceNo\":\"V1\",\"RetCode\":\"S\",\"resCode\":\"0000\"}"
                ));
    }

    @Test
    void validationCheck_whenAnyFieldIsBlank_thenReturnFailandResponse() throws Exception {

        mockMvc.perform(post("/metering/v1/metering")
                .content("{\n" +
                        "  \"serviceNo\":\"V1\",\n" +
                        "  \"carId\":\"CAR123456\",\n" +
                        "  \"hpId\":\"\",\n" +
                        "  \"reqUrl\":\"/ccsp/window.do\"\n" +
                        "}")
                .header("XTID", "testxtid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(
                        "{\"ServiceNo\":\"V1\",\"RetCode\":\"F\",\"resCode\":\"S999\"}"
                ));

    }

    @Test
    void checkAccess_whenControllerDeniesAccess_forBlockedCustomer_thenReturnFailandResponse() throws Exception {
        when(meteringService.checkAccess(any(),anyString())).thenReturn(1);

        makeRequest().andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"ServiceNo\":\"V1\",\"RetCode\":\"S\",\"resCode\":\"0000\"}"
                ));
    }

    @Test
    public void checkAccess_callsUseCase_withApiRequestDataFromBody() throws Exception {
        when(meteringService.checkAccess(any(),anyString())).thenReturn(0);

        // Act
        makeRequest()
                // Assert
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"ServiceNo\":\"V1\",\"RetCode\":\"S\",\"resCode\":\"0000\"}"
                ));

        verify(meteringService).checkAccess(meteringCheckRequest,"testxtid");
    }

    @Test
    public void checkAccess_whenServiceDeniesAccess_returnsFailureResponse() throws Exception {
        // Arrange
        when(meteringService.checkAccess(any(),anyString())).thenReturn(1);

        // Act
        makeRequest()
                .andExpect(content().json(
                        "{\"ServiceNo\":\"V1\",\"RetCode\":\"S\",\"resCode\":\"0000\"}"
                ));
    }

    @Test
    public void checkAccess_whenControllerAllowsAccess_whencaridNull() throws Exception {
        when(meteringService.checkAccess(any(),anyString())).thenReturn(0);

        mockMvc.perform(post("/metering/v1/metering")
                .content("{\n" +
                        "  \"serviceNo\":\"V1\",\n" +
                        "  \"carId\":\"\",\n" +
                        "  \"hpId\":\"HP123456\",\n" +
                        "  \"reqUrl\":\"/pushhistorylist.do\"\n" +
                        "}")
                .header("XTID", "testxtid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"ServiceNo\":\"V1\",\"RetCode\":\"S\",\"resCode\":\"0000\"}"
                ));

    }

    // TODO: Validation check, or in service?

    private ResultActions makeRequest() throws Exception {
        return mockMvc.perform(post("/metering/v1/metering").header("XTID","testxtid")
                .content("{\n" +
                "\"serviceNo\":  \"V1\",\n" +
                "  \"hpId\":  \"HP123456\",\n" +
                "  \"carID\":  \"CAR123456\",\n" +
                "  \"reqUrl\":  \"/ccsp/window.do\",\n" +
                "  \"resObj\" :  null\n" +
                "}").contentType(MediaType.APPLICATION_JSON)
        );
    }
}
