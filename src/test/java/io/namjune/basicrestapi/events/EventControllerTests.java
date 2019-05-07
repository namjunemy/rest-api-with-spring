package io.namjune.basicrestapi.events;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void 이벤트_생성_201() throws Exception {
        EventRequestDto event = EventRequestDto.builder()
            .name("REST API with Spring")
            .description("REST API Basic")
            .beginEnrollmentDateTime(LocalDateTime.of(2019, 5, 6, 17, 0, 0))
            .closeEnrollmentDateTime(LocalDateTime.of(2019, 5, 9, 17, 0, 0))
            .beginEventDateTime(LocalDateTime.of(2019, 5, 10, 17, 0, 0))
            .endEventDateTime(LocalDateTime.of(2019, 5, 13, 17, 0, 0))
            .basePrice(100)
            .maxPrice(100)
            .limitOfEnrollment(100)
            .location("서울대입구")
            .build();

        mockMvc.perform(
            post("/api/events")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("id").exists())
            .andExpect(header().exists(HttpHeaders.LOCATION))
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            //임의로 아이디를 세팅할 수 없음. 다른 값들도 마찬가지.
            .andExpect(jsonPath("id").value(Matchers.not(100L)))
            .andExpect(jsonPath("free").value(Matchers.not(true)))
            .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
        ;
    }

    @Test
    public void 이벤트_생성_BadRequest() throws Exception {
        Event event = Event.builder()
            .id(100L)
            .name("REST API with Spring")
            .description("REST API Basic")
            .beginEnrollmentDateTime(LocalDateTime.of(2019, 5, 6, 17, 0, 0))
            .closeEnrollmentDateTime(LocalDateTime.of(2019, 5, 9, 17, 0, 0))
            .beginEventDateTime(LocalDateTime.of(2019, 5, 10, 17, 0, 0))
            .endEventDateTime(LocalDateTime.of(2019, 5, 13, 17, 0, 0))
            .basePrice(100)
            .maxPrice(100)
            .limitOfEnrollment(100)
            .location("서울대입구")
            .free(true)
            .offline(false)
            .eventStatus(EventStatus.PUBLISHED)
            .build();

        mockMvc.perform(
            post("/api/events")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event)))
            .andDo(print())

            // ObjectMapper가 JSON요청을 RequestDTO로 deserialize하지 못할 때(받지 않아야 할 필드를 받아서)
            // yml 환경설정에서 fail-on-unknown-properties를 true로 주면 BadRequest 응답으로 내려감
            .andExpect(status().isBadRequest())
        ;
    }

    @Test
    public void 이벤트_생성_BadRequest_RequestDTO_이외의필드() throws Exception {
        EventRequestDto eventRequestDto = EventRequestDto.builder().build();

        this.mockMvc.perform(
            post("/api/events")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)
                .content(this.objectMapper.writeValueAsString(eventRequestDto)))
            .andExpect(status().isBadRequest());
    }
}
