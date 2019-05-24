package io.namjune.basicrestapi.common;


import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import io.namjune.basicrestapi.index.IndexController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.validation.Errors;

/**
 * 에러 응답시 모든 API의 진입점인 index 정보를 내려주기 위해. 생성된 Errors 객체를 Resource 객체로 감싸기 위함
 *
 * 주의할 점은 Resource<T> 의 getContent()에 선언된 @JsonUnwrapped 가 json Array는 적용되지 않는다.
 * 따라서, Errors 객체가 content 아래에 래핑되서 응답이 내려가고, 테스트 코드를 수정해야하는 사항이 발생하기도 한다.
 * Serializer 재정의로도 해결 가능.
 *
 */
public class ErrorsResource extends Resource<Errors> {

    public ErrorsResource(Errors content, Link... links) {
        super(content, links);
        add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
    }
}
