package io.namjune.basicrestapi.events;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import io.namjune.basicrestapi.common.ErrorsResource;
import java.net.URI;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

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
        ControllerLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(savedEvent.getId());
        URI createdUri = selfLinkBuilder.toUri();

        EventResource eventResource = new EventResource(event);
        //self link는 매번 API 마다 추가 해야하므로 EventResource에서 공통 처리
        eventResource.add(linkTo(EventController.class).withRel("query-events"));
        eventResource.add(selfLinkBuilder.withRel("update-event"));
        eventResource.add(new Link("/docs/index.html#resources-events-create").withRel("profile"));

        return ResponseEntity.created(createdUri).body(eventResource);
    }

    /**
     * 이벤트 조회
     *
     * @param pageable
     * @param assembler 페이지를 리소스 객체로 바꿔서 링크정보를 추가할 때 유용하게 사용되는 Spring-Data-JPA가 제공하는 객체
     * @return
     */
    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler<Event> assembler) {
        Page<Event> page = this.eventRepository.findAll(pageable);

        // repository에서 받아온 page를 리소스 객체로 변경
        PagedResources<Resource<Event>> pagedResources = assembler.toResource(page);
        return ResponseEntity.ok(pagedResources);
    }

    private ResponseEntity badRequest(Errors errors) {
        //에러 발생시 errors객체만 던지지 않고,API index가 포함된 리소스 객체 리턴
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }
}
