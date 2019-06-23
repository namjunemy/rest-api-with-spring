package io.namjune.basicrestapi.events;

import io.namjune.basicrestapi.accounts.Account;
import io.namjune.basicrestapi.accounts.CurrentUser;
import io.namjune.basicrestapi.common.ErrorsResource;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
@RequiredArgsConstructor
public class EventController {

    private static final ControllerLinkBuilder ROOT_LINK_BUILDER = linkTo(EventController.class);

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    /**
     * 이벤트 생성
     *
     * @param eventRequestDto 요청 객체
     * @param errors          에러 객체
     * @return ResponseEntity
     */
    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventRequestDto eventRequestDto,
                                      Errors errors,
                                      @CurrentUser Account account) {
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        eventValidator.validate(eventRequestDto, errors);
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        Event event = modelMapper.map(eventRequestDto, Event.class);
        event.updateDynamicField();
        event.setManager(account);  // 현재 유저를 이벤트의 매니저로 등록
        Event savedEvent = this.eventRepository.save(event);

        //HATEOAS link 추가
        ControllerLinkBuilder selfLinkBuilder = ROOT_LINK_BUILDER.slash(savedEvent.getId());
        URI createdUri = selfLinkBuilder.toUri();

        EventResource eventResource = new EventResource(event);
        //self link는 매번 API 마다 추가 해야하므로 EventResource에서 공통 처리
        eventResource.add(ROOT_LINK_BUILDER.withRel("query-events"));
        eventResource.add(selfLinkBuilder.withRel("update-event"));
        eventResource.add(new Link("/docs/index.html#resources-events-create").withRel("profile"));

        return ResponseEntity.created(createdUri).body(eventResource);
    }

    /**
     * 이벤트 목록 조회
     *
     * @param pageable  페이징 요청 객체
     * @param assembler 페이지를 리소스 객체로 바꿔서 링크정보를 추가할 때 유용하게 사용되는 Spring-Data-JPA가 제공하는 객체
     * @return ResponseEntity
     */
    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler<Event> assembler,
                                      @CurrentUser Account account) {
        // Spring Expression Language 를 사용하면 @AuthenticationPrincipal 를 사용해서 스프링 시큐리티의 User를 받는 과정에서
        // 시큐리티의 User를 상속 받은 AccountAdapter의 필드인 account를 바로 받을 수 있다.

        // 테스트코드를 디버거로 잡아서 Authentication 안에 있는 스프링 시큐리티가 제공하는 User(스프링 시큐리티의 유저)
        // 정보를 통해서 username을 접근할 수 도 있다. 우리의 목표는 사용자를 우리의 Entity인 Account로 받는 것이다.
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        User principal = (User) authentication.getPrincipal();

        Page<Event> page = this.eventRepository.findAll(pageable);

        // repository에서 받아온 page를 리소스 객체로 변경
        PagedResources<Resource<Event>> pagedResources = assembler.toResource(page, e -> new EventResource(e));
        pagedResources.add(new Link("/docs/index.html#resources-events-list").withRel("profile"));

        // 하지만 @AuthenticationPrincipal 를 사용하면 스프링 시큐리티의 User를 바로 받을 수 있다.
        // 스프링 시큐리티의 유저를 받아서 로그인 사용자일 경우 이벤트 생성 링크를 넣어준다.
        if (account != null) {
            pagedResources.add(linkTo(EventController.class).withRel("create-event"));
        }

        return ResponseEntity.ok()
            .header("Location", String.valueOf(ROOT_LINK_BUILDER.toUri()))
            .body(pagedResources);
    }

    /**
     * 이벤트 조회
     *
     * @param id 이벤트 id
     * @return ResponseEntity
     */
    @GetMapping("/{id}")
    public ResponseEntity getEvent(@PathVariable Long id,
                                   @CurrentUser Account currentUser) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if (!optionalEvent.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Event event = optionalEvent.get();
        EventResource eventResource = new EventResource(event);
        eventResource.add(ROOT_LINK_BUILDER.withRel("query-events"));
        eventResource.add(new Link("/docs/index.html#resources-events-get").withRel("profile"));

        // 현재 사용자와 이벤트의 매니저가 같은 경우에만 업데이트 링크 제공
        if (event.getManager().equals(currentUser)) {
            eventResource.add(ROOT_LINK_BUILDER.slash(id).withRel("update-event"));
        }

        return ResponseEntity.ok()
            .header("Location", String.valueOf(ROOT_LINK_BUILDER.slash(id).toUri()))
            .body(eventResource);
    }

    /**
     * 이벤트 수정
     *
     * @param id              이벤트 id
     * @param eventRequestDto 수정 요청 정보
     * @return ResponseEntity
     */
    @PutMapping("/{id}")
    public ResponseEntity updateEvent(@PathVariable Long id,
                                      @RequestBody @Valid EventRequestDto eventRequestDto,
                                      Errors errors,
                                      @CurrentUser Account currentUser) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if (!optionalEvent.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        this.eventValidator.validate(eventRequestDto, errors);
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        // @Transactional 범위에 들어있지 않기 때문에, 명시적으로 save 호출
        Event existingEvent = optionalEvent.get();

        // 이벤트를 가져왔는데, 이벤트 매니저가 현재 유저가 아니다. 예외 처리
        if (!existingEvent.getManager().equals(currentUser)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        this.modelMapper.map(eventRequestDto, existingEvent);

        Event updatedEvent = this.eventRepository.save(existingEvent);
        EventResource eventResource = new EventResource(updatedEvent);
        eventResource.add(ROOT_LINK_BUILDER.withRel("query-events"));
        eventResource.add(ROOT_LINK_BUILDER.slash(id).withRel("get-event"));
        eventResource.add(new Link("/docs/index.html#resources-events-update").withRel("profile"));

        return ResponseEntity.ok()
            .header("Location", String.valueOf(ROOT_LINK_BUILDER.slash(id).toUri()))
            .body(eventResource);
    }

    private ResponseEntity badRequest(Errors errors) {
        //에러 발생시 errors객체만 던지지 않고,API index가 포함된 리소스 객체 리턴
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }
}
