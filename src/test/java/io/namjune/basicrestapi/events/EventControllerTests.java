package io.namjune.basicrestapi.events;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.namjune.basicrestapi.common.RestDocsConfiguration;
import io.namjune.basicrestapi.common.TestDescription;
import java.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)    //REST Docs prettyPrint
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @TestDescription("정상적으로 이벤트를 생성하는 테스트")
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
            .andExpect(jsonPath("free").value(false))
            .andExpect(jsonPath("offline").value(true))
            .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
            //HATEOAS 만족 시키는지
            .andExpect(jsonPath("_links.self").exists())
            .andExpect(jsonPath("_links.query-events").exists())
            .andExpect(jsonPath("_links.update-event").exists())
            //REST Docs - 기본(요청 본문, 응답 본문 문서화)
            .andDo(document("create-event",
                //링크 문서화 - links snippet 추가
                links(
                    linkWithRel("self").description("link to self"),
                    linkWithRel("query-events").description("link to query events"),
                    linkWithRel("update-event").description("link to update an existing")
                ),
                //요청 헤더 문서화
                requestHeaders(
                    headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                    headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                ),
                //요청 필드 문서화
                requestFields(
                    fieldWithPath("name").description("Name of new event"),
                    fieldWithPath("description").description("Description of new event"),
                    fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                    fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                    fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                    fieldWithPath("endEventDateTime").description("date time of end of new event"),
                    fieldWithPath("location").description("Location of new event"),
                    fieldWithPath("basePrice").description("Base Price of new event"),
                    fieldWithPath("maxPrice").description("Max Price of new event"),
                    fieldWithPath("limitOfEnrollment").description("Limit of enrollment")
                )
            ))
        ;
    }

    @Test
    @TestDescription("입력 받을 수 없는 값을 사용하는 경우 BadRequest 발생하는 테스트")
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
    @TestDescription("입력 값이 비어있는 경우에 에러가 발생하는 테스트")
    public void 이벤트_생성_BadRequest_RequestDTO_이외의필드() throws Exception {
        EventRequestDto eventRequestDto = EventRequestDto.builder().build();

        this.mockMvc.perform(
            post("/api/events")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)
                .content(this.objectMapper.writeValueAsString(eventRequestDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력 값이 잘못된 경우에 에러가 발생하는 테스트")
    public void 이벤트_생성_BadRequest_데이터값이_이상할_때_커스텀_Validator로_처리() throws Exception {
        EventRequestDto eventRequestDto = EventRequestDto.builder()
            .name("REST API with Spring")
            .description("REST API Basic")
            .beginEnrollmentDateTime(LocalDateTime.of(2019, 5, 9, 17, 0, 0))
            .closeEnrollmentDateTime(LocalDateTime.of(2019, 5, 6, 17, 0, 0))
            .beginEventDateTime(LocalDateTime.of(2019, 5, 10, 17, 0, 0))
            .endEventDateTime(LocalDateTime.of(2019, 5, 13, 17, 0, 0))
            .basePrice(10000)
            .maxPrice(100)
            .limitOfEnrollment(100)
            .location("서울대입구")
            .build();

        this.mockMvc.perform(
            post("/api/events")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)
                .content(this.objectMapper.writeValueAsString(eventRequestDto)))
            .andExpect(status().isBadRequest())
            // 응답 본문으로 Bad Request 정보를 받기 위해서 스프링에서 제공하는 Errors 객체를 그냥 넘기면
            // Errors가 넘어 올 때, Error를 serialization 해줄 수 있는 Serializer가
            // ObjectMapper가 등록 되어있지 않기 때문에 beanSerialize 에러 발생
            // 처리하기 위해 @JsonComponent ErrorSerializer ObjectMapper에 등록해서 변환 처리
            // 이렇게 처리하면 REST API를 사용하는 클라이언트에서 응답 본문만 보고 핸들링 가능
            .andDo(print())
            .andExpect(jsonPath("$[0].objectName").exists())
            .andExpect(jsonPath("$[0].defaultMessage").exists())
            .andExpect(jsonPath("$[0].code").exists())
        ;
    }
}