package io.namjune.basicrestapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.namjune.basicrestapi.common.RestDocsConfiguration;
import io.namjune.basicrestapi.common.TestDescription;
import java.util.stream.IntStream;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)    //REST Docs prettyPrint
@ActiveProfiles("test")
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EventRepository eventRepository;

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

            //HATEOAS 만족 시키는지 -> document에서 테스트를 했기 때문에 HATEOAS 테스트는 하지 않아도 된다.
//            .andExpect(jsonPath("_links.self").exists())
//            .andExpect(jsonPath("_links.query-events").exists())
//            .andExpect(jsonPath("_links.update-event").exists())

            //REST Docs - 기본(요청 본문, 응답 본문 문서화)
            .andDo(document("create-event",
                //링크 문서화 - links snippet 추가
                links(
                    linkWithRel("self").description("link to self"),
                    linkWithRel("query-events").description("link to query events"),
                    linkWithRel("update-event").description("link to update an existing"),
                    linkWithRel("profile").description("link to profile an existing")
                ),
                //요청 헤더 문서화
                requestHeaders(
                    headerWithName(HttpHeaders.ACCEPT).description("Accept header"),
                    headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
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
                ),
                //응답 헤더 문서화
                responseHeaders(
                    headerWithName(HttpHeaders.LOCATION).description("Location header"),
                    headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                ),
                //응답 필드 문서화
                //links까지 응답 필드로 보기때문에 releaxed 키워드를 붙이지 않으면 에러 발생
                // relaxed prefix의 장점 - 일부분만 테스트 할 수 있다.
                //                  단점 - 정확한 문서를 생성하지 못한다.
                // 그냥 링크 필드를 추가하는 방법도 있다.(권장)
                //relaxedResponseFields(
                responseFields(
                    fieldWithPath("id").description("Identifier of new event"),
                    fieldWithPath("name").description("Name of new event"),
                    fieldWithPath("description").description("Description of new event"),
                    fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                    fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                    fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                    fieldWithPath("endEventDateTime").description("date time of end of new event"),
                    fieldWithPath("location").description("Location of new event"),
                    fieldWithPath("basePrice").description("Base Price of new event"),
                    fieldWithPath("maxPrice").description("Max Price of new event"),
                    fieldWithPath("limitOfEnrollment").description("Limit of enrollment"),
                    fieldWithPath("free").description("Event is free or not"),
                    fieldWithPath("offline").description("Event is offline meeting or not"),
                    fieldWithPath("eventStatus").description("Event status"),
                    fieldWithPath("_links.self.href").description("link to self"),
                    fieldWithPath("_links.query-events.href").description("link to query events"),
                    fieldWithPath("_links.update-event.href").description("link to update an existing"),
                    fieldWithPath("_links.profile.href").description("link to profile an existing")
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
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("_links.index").exists());
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
            .andExpect(jsonPath("content[0].objectName").exists())
            .andExpect(jsonPath("content[0].defaultMessage").exists())
            .andExpect(jsonPath("content[0].code").exists())
            .andExpect(jsonPath("_links.index").exists())
        ;
    }

    @Test
    @TestDescription("30개의 이벤트를 10개씩, 그 중 두번째 페이지 조회하기")
    public void queryEvents() throws Exception {
        // Given
        IntStream.range(0, 30).forEach(this::generatedEvent);

        // When
        this.mockMvc.perform(
            get("/api/events")
                .param("page", "1")
                .param("size", "10")
                .param("sort", "name,DESC"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("page").exists())
            .andExpect(jsonPath("_links").exists())
        ;

        // Then

    }

    private void generatedEvent(int index) {
        Event event = Event.builder()
            .name("event " + index)
            .description("test event")
            .build();

        this.eventRepository.save(event);
    }
}