package io.namjune.basicrestapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

/**
 * Auth Server 와 연동 돼서 사용된다.
 * 리소스 서버는 토큰을 기반으로 인증정보가 있는지 없는지 확인하고, 리소스 서버에 접근을 제한하는 일을 한다.
 * 그래서 엄밀히 말하자면 리소스 서버는 Event 리소스를 제공하는 서버와 있는게 맞고,
 * 인증 서버는 분리되는 것이 맞다.
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        // 리소스 id 설, 나머지는 기본
        resources.resourceId("event");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.anonymous()

            .and()
            .authorizeRequests()
            .mvcMatchers(HttpMethod.GET, "/api/**").permitAll()
            .anyRequest().authenticated()

            .and()
            .exceptionHandling()
            .accessDeniedHandler(new OAuth2AccessDeniedHandler());
    }
}
