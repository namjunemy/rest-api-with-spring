package io.namjune.basicrestapi.accounts;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 커스텀 어노테이션 정의
 */
@Target(ElementType.PARAMETER)  // 어노테이션 적용 위치
@Retention(RetentionPolicy.RUNTIME) // 어노테이션 유지 기간
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : account")  // 인증정보가 anonymousUser 일 경우 예외 처리
public @interface CurrentUser {
}
