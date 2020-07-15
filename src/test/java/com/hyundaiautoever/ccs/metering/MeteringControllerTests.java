package com.hyundaiautoever.ccs.metering;

import com.hyundaiautoever.ccs.metering.VO.MeteringCheckRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {MeteringController.class})
public class MeteringControllerTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MeteringService meteringService;

    MeteringCheckRequest meteringCheckRequest = MeteringCheckRequest.builder()
            .serviceNo("V1")
            .carId("CAR123456")
            .hpId("HP123456")
            .reqUrl("/ccsp/window.do")
            .build();

    @Test
    void checkAccess_whenControllerAllowsAccess_thenReturnSuccess() throws Exception {
        when(meteringService.checkAccess(any())).thenReturn(true);

        makeRequest().andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"ServiceNo\":\"V1\",\"RetCode\":\"S\",\"resCode\":\"0000\"}"
                ));
    }

    @Test
    void validationCheck_whenAnyFieldIsBlank_thenReturnFailandResponse() throws Exception {

        mockMvc.perform(post("/metering")
                .content("{\n" +
                        "  \"serviceNo\": \"V1\",\n" +
                        "  \"carId\":  \"CAR123456\",\n" +
                        "  \"hpId\": \"\",\n" +
                        "  \"reqUrl\": \"/ccsp/window.do\"\n" +
                        "  \n" +
                        "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        "{\"ServiceNo\":\"V1\",\"RetCode\":\"F\",\"resCode\":\"S999\"}"
                ));

    }

    @Test
    void checkAccess_whenControllerDeniesAccess_forBlockedCustomer_thenReturnFailandResponse() throws Exception {
        when(meteringService.checkAccess(any())).thenReturn(false);

        makeRequest().andExpect(status().isTooManyRequests())
                .andExpect(content().json(
                        "{\"ServiceNo\":\"V1\",\"RetCode\":\"F\",\"resCode\":\"BK02\"}"
                ));
    }

    @Test
    public void checkAccess_callsUseCase_withApiRequestDataFromBody() throws Exception {
        when(meteringService.checkAccess(any())).thenReturn(true);

        // Act
        makeRequest()
                // Assert
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"ServiceNo\":\"V1\",\"RetCode\":\"S\",\"resCode\":\"0000\"}"
                ));

        verify(meteringService).checkAccess(meteringCheckRequest);
    }

    @Test
    public void checkAccess_whenServiceDeniesAccess_returnsFailureResponse() throws Exception {
        // Arrange
        when(meteringService.checkAccess(meteringCheckRequest)).thenReturn(false);

        // Act
        makeRequest()
                .andExpect(content().json(
                        "{\"ServiceNo\":\"V1\",\"RetCode\":\"F\",\"resCode\":\"BK02\"}"
                ));
    }


    // TODO: Validation check, or in service?

    private ResultActions makeRequest() throws Exception {
        return mockMvc.perform(post("/metering").content("{\n" +
                "\"serviceNo\":  \"V1\",\n" +
                "  \"hpId\":  \"HP123456\",\n" +
                "  \"carId\":  \"CAR123456\",\n" +
                "  \"reqUrl\":  \"/ccsp/window.do\",\n" +
                "  \"resObj\" :  null\n" +
                "}").contentType(MediaType.APPLICATION_JSON));
    }
}
