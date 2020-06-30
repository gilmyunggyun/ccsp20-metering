package com.hyundaiautoever.ccs.metering;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

    @Test
    public void checkAccess_callsUseCase_withApiRequestDataFromBody() throws Exception {
        // Arrange
        when(meteringService.checkAccess(any(), any(), any(), any()))
                .thenReturn(true);

        // Act
        mockMvc.perform(post("/metering").content("{\n" +
                "\"serviceNo\":  \"V1\",\n" +
                "  \"hpId\":  \"HP1234\",\n" +
                "  \"carId\":  \"CAR1234\",\n" +
                "  \"reqUrl\":  \"/ccsp/window.do\",\n" +
                "  \"resObj\" :  null\n" +
                "}").contentType(MediaType.APPLICATION_JSON))

        // Assert
                .andExpect(status().isOk())
                .andExpect(content().json("{\"serviceNo\":\"V1\",\"retCode\":\"S\",\"resCode\":\"S000\"}"));

        verify(meteringService).checkAccess("V1", "HP1234", "CAR1234", "/ccsp/window.do");
    }

    @Test
    public void checkAccess_whenServiceDeniesAccess_returnsFailureResponse() throws Exception {
        // Arrange
        when(meteringService.checkAccess(any(), any(), any(), any()))
                .thenReturn(false);

        // Act
        mockMvc.perform(post("/metering").content("{\n" +
                "\"serviceNo\":  \"V1\",\n" +
                "  \"hpId\":  \"HP1234\",\n" +
                "  \"carId\":  \"CAR1234\",\n" +
                "  \"reqUrl\":  \"/ccsp/window.do\",\n" +
                "  \"resObj\" :  null\n" +
                "}").contentType(MediaType.APPLICATION_JSON))

        // Assert
                .andExpect(status().isOk())
                .andExpect(content().json("{\"serviceNo\":\"V1\",\"retCode\":\"F\",\"resCode\":\"BK02\"}"));
    }

    // TODO: Validation check, or in service?
}
