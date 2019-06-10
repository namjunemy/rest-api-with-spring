package io.namjune.basicrestapi.config;

import io.namjune.basicrestapi.accounts.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

/**
 * @EnableWebSecurity 를 선언하고, WebSecurityConfigurerAdapter 를 상속 받는 순간
 * 스프링 부트의 시큐리티 자동 설정은 적용되지 않는다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public TokenStore tokenStore() {
        return new InMemoryTokenStore();
    }

    /**
     * 이 메서드를 오버라이드 해서 빈으로 선언해야
     * 다른 곳에서 AuthenticationManager 를 사용할 수 있다.
     *
     * @return AuthenticationManager
     * @throws Exception
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * AuthenticationManager 를 어떻게 만들것인가? 에 대한 설정
     * 내가 만든 userDetailsService, passwordEncoder 를 등록
     *
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(accountService)
            .passwordEncoder(passwordEncoder);
    }

    /**
     * 시큐리티 필터 적용 여부. 시큐리티가 적용되지 않도록 web 에서 걸러낼 수 있음(http 전에)
     * 1. REST Docs 경로에 대한 필터 처리 예외 적용
     * 2. 스프링 부트가 제공하는 static 리소스들의 기본위치를 가져와서 필터 처리 예외를 적용
     *
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().mvcMatchers("/docs/index.html");
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    /**
     * web 에서 거르지 않고,
     * 스프링 시큐리티 영역으로 들어와서 시큐리티 필터를 타게 하면서 http 요청을 거를 수 있다.
     * 그러나 static이나, 아예 허용해줄 리소스라면 조금이라도 일을 덜 하도록 web 에서 걸러주는게 더 낫다.
     *
     * @param http
     * @throws Exception
     */
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.authorizeRequests()
//            .mvcMatchers("/docs/index.html").anonymous()
//            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).anonymous();
//    }
}
