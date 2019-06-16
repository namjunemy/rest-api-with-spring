package io.namjune.basicrestapi.config;

import io.namjune.basicrestapi.accounts.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * 인증 서버 설정
 *
 * 인증에 필요한 토큰을 발급하는 서버 역할을 한다.
 * 토큰을 가지고 리소스에 접근할 때 즉, 이벤트를 가지고 무언가를 할때에 어떻게 토큰을 사용할지는 리소스 서버에서 정한다.
 */
@Configuration
@EnableAuthorizationServer
@RequiredArgsConstructor
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AccountService accountService;
    private final TokenStore tokenStore;
    private final AppProperties appProperties;

    /**
     * 인증 서버로 넘어오는 client의 secret도 encoding해서 관리 하도록 설정(유저의 계정에 있는 password와 마찬가지로)
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.passwordEncoder(passwordEncoder);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // 라이브에서는 jdbc로 DB로 관리
//        clients.jdbc()

        // 현재는 인메모리로 테스트
        clients.inMemory()
            .withClient(appProperties.getClientId())
            .authorizedGrantTypes("password", "refresh_token")
            .scopes("read", "write")
            .secret(this.passwordEncoder.encode(appProperties.getClientSecret()))
            .accessTokenValiditySeconds(10 * 60)
            .refreshTokenValiditySeconds(6 * 10 * 60);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        // SecurityConfig 에서 빈으로 등록해논 것을 설정
        endpoints.authenticationManager(authenticationManager)
            .userDetailsService(accountService)
            .tokenStore(tokenStore);
    }
}
