package io.namjune.basicrestapi.events;

import io.namjune.basicrestapi.accounts.Account;
import io.namjune.basicrestapi.accounts.AccountRepository;
import io.namjune.basicrestapi.accounts.AccountRole;
import io.namjune.basicrestapi.accounts.AccountService;
import io.namjune.basicrestapi.common.BaseControllerTest;
import io.namjune.basicrestapi.common.TestDescription;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventControllerTests extends BaseControllerTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Before
    public void setUp() {
        this.eventRepository.deleteAll();
        this.accountRepository.deleteAll();
    }

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
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
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
                    fieldWithPath("manager").description("Account"),
                    fieldWithPath("_links.self.href").description("link to self"),
                    fieldWithPath("_links.query-events.href").description("link to query events"),
                    fieldWithPath("_links.update-event.href").description("link to update event"),
                    fieldWithPath("_links.profile.href").description("link to profile")
                )
            ))
        ;
    }

    private String getBearerToken() throws Exception {
        return "Bearer " + getAccessToken();
    }

    private String getAccessToken() throws Exception {
        // Given
        String username = "test@gmail.com";
        String password = "1234";

        Account account = Account.builder()
            .email(username)
            .password(password)
            .roles(Stream.of(AccountRole.ADMIN, AccountRole.USER).collect(Collectors.toSet()))
            .build();
        this.accountService.savePasswordEncodedAccount(account);

        String clientId = "myApp";
        String clientSecret = "pass";

        // 인증서버가 등록이 되면 기본적으로 '/oauth/token' 요청을 처리할 수 있는 핸들러가 등록된다.
        ResultActions perform = this.mockMvc.perform(
            MockMvcRequestBuilders.post("/oauth/token")
                .with(httpBasic(clientId, clientSecret))    //HTTP basic 인증 헤더
                .param("username", username)
                .param("password", password)
                .param("grant_type", "password")
        );

        //응답을 바탕으로 access token 파싱
        MockHttpServletResponse response = perform.andReturn().getResponse();
        String responseBody = response.getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();
        return parser.parseMap(responseBody).get("access_token").toString();
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
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
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
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
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
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
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

        // When & Then
        this.mockMvc.perform(
            get("/api/events")
                .param("page", "1")
                .param("size", "10")
                .param("sort", "name,DESC"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("page").exists())
            .andExpect(jsonPath("_links").exists()) //페이징 관련 링크 정보 검증
            .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists()) // 각 이벤트 self URL 리턴 검증
            .andExpect(jsonPath("_links.self").exists())    //self 링크 검증
            .andExpect(jsonPath("_links.profile").exists()) //profile 링크 검증
            .andDo(document("query-events",
                //요청 파라미터 문서화
                requestParameters(
                    parameterWithName("page").description("페이지. 첫 페이지 = 0"),
                    parameterWithName("size").description("페이지당 컨텐츠 수"),
                    parameterWithName("sort").description("정렬 값 / format => [대상 필드, 순서] / ex => [name,DESC]")
                ),
                //링크 문서화
                links(
                    linkWithRel("first").description("첫 페이지"),
                    linkWithRel("prev").description("이전 페이지"),
                    linkWithRel("self").description("현재 페이지"),
                    linkWithRel("next").description("다음 페이지"),
                    linkWithRel("last").description("마지막 페이지"),
                    linkWithRel("profile").description("link to profile an existing")
                ),
                //응답 헤더 문서화
                responseHeaders(
                    headerWithName(HttpHeaders.LOCATION).description("Location header"),
                    headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                ),
                //응답 필드 문서화
                responseFields(
                    fieldWithPath("_embedded.eventList[].id").description("Identifier of new event"),
                    fieldWithPath("_embedded.eventList[].name").description("Name of new event"),
                    fieldWithPath("_embedded.eventList[].description").description("Description of new event"),
                    fieldWithPath("_embedded.eventList[].beginEnrollmentDateTime").description("date time of begin of new event"),
                    fieldWithPath("_embedded.eventList[].closeEnrollmentDateTime").description("date time of close of new event"),
                    fieldWithPath("_embedded.eventList[].beginEventDateTime").description("date time of begin of new event"),
                    fieldWithPath("_embedded.eventList[].endEventDateTime").description("date time of end of new event"),
                    fieldWithPath("_embedded.eventList[].location").description("Location of new event"),
                    fieldWithPath("_embedded.eventList[].basePrice").description("Base Price of new event"),
                    fieldWithPath("_embedded.eventList[].maxPrice").description("Max Price of new event"),
                    fieldWithPath("_embedded.eventList[].limitOfEnrollment").description("Limit of enrollment"),
                    fieldWithPath("_embedded.eventList[].free").description("Event is free or not"),
                    fieldWithPath("_embedded.eventList[].offline").description("Event is offline meeting or not"),
                    fieldWithPath("_embedded.eventList[].eventStatus").description("Event status"),
                    fieldWithPath("_embedded.eventList[].manager").description("Account"),
                    fieldWithPath("_embedded.eventList[]._links.self.href").description("link to self"),
                    fieldWithPath("_links.first.href").description("첫 페이지"),
                    fieldWithPath("_links.prev.href").description("이전 페이지"),
                    fieldWithPath("_links.self.href").description("현재 페이지"),
                    fieldWithPath("_links.next.href").description("다음 페이지"),
                    fieldWithPath("_links.last.href").description("마지막 페이지"),
                    fieldWithPath("_links.profile.href").description("link to profile an existing"),
                    fieldWithPath("page.size").description("요청한 페이징 단위"),
                    fieldWithPath("page.totalElements").description("조회된 총 응답 객체 수"),
                    fieldWithPath("page.totalPages").description("조회된 총 페이지 수"),
                    fieldWithPath("page.number").description("요청한 페이지 넘버")
                )
            ))
        ;
    }

    @Test
    @TestDescription("인증정보 포함해서 요청 > 30개의 이벤트를 10개씩, 그 중 두번째 페이지 조회하기")
    public void queryEventsWithAuthentication() throws Exception {
        // Given
        IntStream.range(0, 30).forEach(this::generatedEvent);

        // When & Then
        this.mockMvc.perform(
            get("/api/events")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .param("page", "1")
                .param("size", "10")
                .param("sort", "name,DESC"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("page").exists())
            .andExpect(jsonPath("_links").exists()) //페이징 관련 링크 정보 검증
            .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists()) // 각 이벤트 self URL 리턴 검증
            .andExpect(jsonPath("_links.self").exists())    //self 링크 검증
            .andExpect(jsonPath("_links.profile").exists()) //profile 링크 검증
            .andExpect(jsonPath("_links.create-event").exists())  // -> 인증 사용자의 경우 CREATE 링크가 있기를 기대함

        ;
    }

    @Test
    @TestDescription("기존의 이벤트를 하나 조회하기")
    public void getEvent() throws Exception {
        // Given
        Event event = this.generatedEvent(100);

        // When & Then
        this.mockMvc.perform(get("/api/events/{id}", event.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("name").exists())
            .andExpect(jsonPath("id").exists())
            .andExpect(jsonPath("_links.self").exists())
            .andExpect(jsonPath("_links.profile").exists())
            .andDo(document("get-event",
                pathParameters(
                    parameterWithName("id").description("이벤트 ID")
                ),
                responseHeaders(
                    headerWithName(HttpHeaders.LOCATION).description("Location header"),
                    headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type")
                ),
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
                    fieldWithPath("manager").description("Account"),
                    fieldWithPath("_links.self.href").description("link to self"),
                    fieldWithPath("_links.query-events.href").description("link to query events"),
                    fieldWithPath("_links.update-event.href").description("link to update event"),
                    fieldWithPath("_links.profile.href").description("link to profile")
                ),
                links(
                    linkWithRel("self").description("link to self"),
                    linkWithRel("query-events").description("link to query events"),
                    linkWithRel("update-event").description("link to update event"),
                    linkWithRel("profile").description("link to profile")
                )
            ))
        ;
    }

    @Test
    @TestDescription("없는 이벤트 조회했을 때 404 status code")
    public void getEvent_404() throws Exception {
        // When & Then
        this.mockMvc.perform(get("/api/events/23456"))
            .andExpect(status().isNotFound());
    }

    /**
     * static import 시 주의사항
     * <p>
     * Documentation시 pathParameters를 사용하는 경우 get, put 등을
     * MockMvcBuilder.get() 보다 RestDocumentationRequestBuilders.get() 을 이용하는 것을 권장한다. 에러난다.
     */
    @Test
    @TestDescription("이벤트를 정상적으로 수정")
    public void updateEvent() throws Exception {
        // Given
        Event event = this.generatedEvent(200);

        EventRequestDto eventRequestDto = this.modelMapper.map(event, EventRequestDto.class);
        String eventName = "Updated Event";
        eventRequestDto.setName(eventName);

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
            .header(HttpHeaders.AUTHORIZATION, getBearerToken())
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaTypes.HAL_JSON)
            .content(this.objectMapper.writeValueAsString(eventRequestDto))
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("id").exists())
            .andExpect(jsonPath("name").value(eventName))
            .andExpect(header().exists(HttpHeaders.LOCATION))
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(document("update-event",
                pathParameters(
                    parameterWithName("id").description("이벤트 ID")
                ),
                requestHeaders(
                    headerWithName(HttpHeaders.ACCEPT).description("Accept header"),
                    headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                ),
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
                responseHeaders(
                    headerWithName(HttpHeaders.LOCATION).description("Location header"),
                    headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                ),
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
                    fieldWithPath("manager").description("Account"),
                    fieldWithPath("_links.self.href").description("link to self"),
                    fieldWithPath("_links.query-events.href").description("link to query events"),
                    fieldWithPath("_links.get-event.href").description("link to update event"),
                    fieldWithPath("_links.profile.href").description("link to profile")
                ),
                links(
                    linkWithRel("self").description("link to self"),
                    linkWithRel("query-events").description("link to query events"),
                    linkWithRel("get-event").description("link to get event"),
                    linkWithRel("profile").description("link to profile")
                )
            ))
        ;
    }

    @Test
    @TestDescription("입력값이 비어있는 경우 이벤트 수정 실패")
    public void updateEvent_400_Empty() throws Exception {
        // Given
        Event event = this.generatedEvent(200);
        EventRequestDto eventRequestDto = new EventRequestDto();

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
            .header(HttpHeaders.AUTHORIZATION, getBearerToken())
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(this.objectMapper.writeValueAsString(eventRequestDto))
        )
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력값이 잘못된 경우 이벤트 수정 실패 - Validation Error")
    public void updateEvent_400_Wrong() throws Exception {
        // Given
        Event event = this.generatedEvent(200);
        EventRequestDto eventRequestDto = this.modelMapper.map(event, EventRequestDto.class);
        eventRequestDto.setBasePrice(20000);
        eventRequestDto.setMaxPrice(1000);

        // When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
            .header(HttpHeaders.AUTHORIZATION, getBearerToken())
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(this.objectMapper.writeValueAsString(eventRequestDto))
        )
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("존재하지 않는 이벤트 수정 실패")
    public void updateEvent_404() throws Exception {
        // Given
        Event event = this.generatedEvent(200);
        EventRequestDto eventRequestDto = this.modelMapper.map(event, EventRequestDto.class);

        // When & Then
        this.mockMvc.perform(put("/api/events/9308")
            .header(HttpHeaders.AUTHORIZATION, getBearerToken())
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(this.objectMapper.writeValueAsString(eventRequestDto))
        )
            .andDo(print())
            .andExpect(status().isNotFound());
    }


    private Event generatedEvent(int index) {
        Event event = Event.builder()
            .name("event " + index)
            .description("test event")
            .beginEnrollmentDateTime(LocalDateTime.of(2019, 5, 6, 17, 0, 0))
            .closeEnrollmentDateTime(LocalDateTime.of(2019, 5, 9, 17, 0, 0))
            .beginEventDateTime(LocalDateTime.of(2019, 5, 10, 17, 0, 0))
            .endEventDateTime(LocalDateTime.of(2019, 5, 13, 17, 0, 0))
            .basePrice(100)
            .maxPrice(100)
            .limitOfEnrollment(100)
            .location("서울대입구")
            .free(false)
            .offline(true)
            .eventStatus(EventStatus.DRAFT)
            .build();

        return this.eventRepository.save(event);
    }
}
