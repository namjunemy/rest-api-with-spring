package io.namjune.basicrestapi.events;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import io.namjune.basicrestapi.common.ErrorsResource;
import java.net.URI;
import java.util.Optional;
import javax.validation.Valid;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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
     * @param errors 에러 객체
     * @return ResponseEntity
     */
    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventRequestDto eventRequestDto,
                                      Errors errors) {
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        eventValidator.validate(eventRequestDto, errors);
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        Event event = modelMapper.map(eventRequestDto, Event.class);
        event.updateDynamicField();
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
     * @param pageable 페이징 요청 객체
     * @param assembler 페이지를 리소스 객체로 바꿔서 링크정보를 추가할 때 유용하게 사용되는 Spring-Data-JPA가 제공하는 객체
     * @return ResponseEntity
     */
    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler<Event> assembler) {
        Page<Event> page = this.eventRepository.findAll(pageable);

        // repository에서 받아온 page를 리소스 객체로 변경
        PagedResources<Resource<Event>> pagedResources = assembler.toResource(page, e -> new EventResource(e));
        pagedResources.add(new Link("/docs/index.html#resources-events-list").withRel("profile"));
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
    public ResponseEntity getEvent(@PathVariable Long id) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if (!optionalEvent.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Event event = optionalEvent.get();
        EventResource eventResource = new EventResource(event);
        eventResource.add(ROOT_LINK_BUILDER.withRel("query-events"));
        eventResource.add(ROOT_LINK_BUILDER.slash(id).withRel("update-event"));
        eventResource.add(new Link("/docs/index.html#resources-events-get").withRel("profile"));

        return ResponseEntity.ok()
            .header("Location", String.valueOf(ROOT_LINK_BUILDER.toUri()))
            .body(eventResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateEvent(@PathVariable Long id,
                                      @RequestBody @Valid EventRequestDto eventRequestDto,
                                      Errors errors) {
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
        this.modelMapper.map(eventRequestDto, existingEvent);

        Event updatedEvent = this.eventRepository.save(existingEvent);
        EventResource eventResource = new EventResource(updatedEvent);
        eventResource.add(ROOT_LINK_BUILDER.withRel("query-events"));
        eventResource.add(ROOT_LINK_BUILDER.slash(id).withRel("get-event"));
        eventResource.add(new Link("/docs/index.html#resources-events-update").withRel("profile"));

        return ResponseEntity.ok(eventResource);
    }

    private ResponseEntity badRequest(Errors errors) {
        //에러 발생시 errors객체만 던지지 않고,API index가 포함된 리소스 객체 리턴
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }
}
