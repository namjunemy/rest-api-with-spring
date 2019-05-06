package io.namjune.basicrestapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@WebMvcTest
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // Controller 테스트에서 필요한 객체(Repository) Mocking
    @MockBean
    EventRepository eventRepository;

    @Test
    public void 이벤트_생성_201() throws Exception {
        Event event = Event.builder()
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

        // Mockito로 Mock 객체(EventRepository) stubbing
        event.setId(1L);
        Mockito.when(eventRepository.save(event)).thenReturn(event);

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
        ;
    }
}
