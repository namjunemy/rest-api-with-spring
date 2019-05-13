package io.namjune.basicrestapi.events;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

//ResourceSupport 를 상속받으면 감싸진 필드(Event) 객체를 BeanSerializer 가 serialization 할 때 감싸진다.
//이걸 막기 위해서 @JsonUnwrapped 을 사용할 수도 있지만 - 첫번째 방법
//ResourceSupport 의 하위에 있는 Resource<T> 를 사용하면 내부적으로 getContent()에 이미 @JsonUnwrapped 가 선언되어 있다.
public class EventResource extends Resource<Event> {

    public EventResource(Event event, Link... links) {
        super(event, links);

        //EventController 의 mappingUrl + / + event.getId로 Self link relation 을 만든다.
        add(linkTo(EventController.class).slash(event.getId()).withSelfRel());
        // == add(new Link("http://localhost:8080/api/events" + event.getId()))
        // linkTo 를 이용해서 만드는 것이 new Link()로 만드는 것 보다 더 Type safe 하고, 컨트롤러의 변경에 대응할 수 있다.
    }
}
