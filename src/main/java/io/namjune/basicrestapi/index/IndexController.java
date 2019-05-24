package io.namjune.basicrestapi.index;


import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import io.namjune.basicrestapi.events.EventController;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    /**
     * 리소스 정보만 리턴하면 되기 때문에 ResourceSupport 타입 사용
     *
     * @return ResourceSupport
     */
    @GetMapping("/api")
    public ResourceSupport index() {
        ResourceSupport resourceSupport = new ResourceSupport();
        resourceSupport.add(linkTo(EventController.class).withRel("events"));
        return resourceSupport;
    }
}
