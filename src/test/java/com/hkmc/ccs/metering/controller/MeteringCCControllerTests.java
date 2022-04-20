package com.hkmc.ccs.metering.controller;

import com.hkmc.ccs.metering.models.vo.MeteringCCBlockList;
import com.hkmc.ccs.metering.models.vo.MeteringCCRequest;
import com.hkmc.ccs.metering.service.MeteringCCService;
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

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("local")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@WebMvcTest(value = {MeteringCCController.class})
public class MeteringCCControllerTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MeteringCCService meteringCCService;

    @MockBean
    @Qualifier("requestResponseLogger")
    Logger requestResponseLogger;

    MeteringCCRequest meteringCheckRequest = MeteringCCRequest.builder()
            .carId("CAR123456")
            .requestId("cust119")
            .build();

    @Test
    void getBlockList_whenControllerAllowsAccess_whenDataEmpty() throws Exception {

        getBlockList_makeRequest().andExpect(status().isBadRequest())
                .andExpect(content().json(
                        "{\"resultCode\":\"F\",\"resultMessage\":\"조회 정보 없음\",\"carId\":\"CAR123456\"}"
                ));
    }

    @Test
    void getBlockList_whenControllerAllowsAccess_whenDataError() throws Exception {
        mockMvc.perform(post("/metering/v1/getBlockList")
                .content("{\n" +
                        "  \"requestId\":  \"cust119\"\n" +
                        "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void getBlockList_whenControllerAllowsAccess_whenSuccess() throws Exception {

        List<MeteringCCBlockList> Resultlist = new ArrayList<MeteringCCBlockList>();

        MeteringCCBlockList meteringCCBlockList = new MeteringCCBlockList();
        meteringCCBlockList.setCcid("CAR123456");
        meteringCCBlockList.setRsonCd("오류");
        meteringCCBlockList.setBlockedDate(OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyMMddHHmmss")));

        Resultlist.add(meteringCCBlockList);


        when(meteringCCService.getBlockList(any())).thenReturn(Resultlist);

        mockMvc.perform(post("/metering/v1/getBlockList")
                .content("{\n" +
                        "  \"carID\":  \"CAR123456\",\n" +
                        "  \"requestId\":  \"cust119\"\n" +
                        "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(
                        "{\"carId\":\"CAR123456\",\"resultCode\":\"S\",\"resultMessage\":\"Success\"}"
                ));

    }



    @Test
    void unblock_whenControllerAllowsAccess_whenDataEmpty() throws Exception {

        unblock_makeRequest().andExpect(status().isBadRequest())
                .andExpect(content().json(
                        "{\"resultCode\":\"F\",\"resultMessage\":\"삭제할 정보 없음\",\"carId\":\"CAR123456\"}"
                ));
    }

    @Test
    void unblock_whenControllerAllowsAccess_whenDataError() throws Exception {

        mockMvc.perform(post("/metering/v1/unblock")
                .content("{\n" +
                        "  \"requestId\":  \"cust119\"\n" +
                        "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }


    private ResultActions getBlockList_makeRequest() throws Exception {
        return mockMvc.perform(post("/metering/v1/getBlockList")
                .content("{\n" +
                        "  \"carID\":  \"CAR123456\",\n" +
                        "  \"requestId\":  \"cust119\"\n" +
                        "}").contentType(MediaType.APPLICATION_JSON)
        );
    }

    private ResultActions unblock_makeRequest() throws Exception {
        return mockMvc.perform(post("/metering/v1/unblock")
                .content("{\n" +
                        "  \"carID\":  \"CAR123456\",\n" +
                        "  \"requestId\":  \"cust119\"\n" +
                        "}").contentType(MediaType.APPLICATION_JSON)
        );
    }
}
