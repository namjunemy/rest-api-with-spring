package io.namjune.basicrestapi.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.namjune.basicrestapi.accounts.Account;
import io.namjune.basicrestapi.accounts.AccountRole;
import io.namjune.basicrestapi.accounts.AccountService;
import io.namjune.basicrestapi.common.BaseControllerTest;
import io.namjune.basicrestapi.common.TestDescription;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AuthServerConfigTest extends BaseControllerTest {

    @Autowired
    AccountService accountService;

    @Autowired
    AppProperties appProperties;

    /**
     * oauth2 가 지원하는 6가지 인증방식 중에 우리는 Password, Refresh Token 두 가지 방식을 지원할 것이다.
     * 최초에 토큰을 발급 받을 때에는 Password 타입으로 발급 받는다.
     * Password 방식은 요청과 응답 한 홉에 의해서 토큰을 발급 받을 수 있다.
     * 이 방식은 username과 password를 직접 사용하기 때문에, 써드파티에게 이 방식을 허용해주면 절대 안된다.
     * 인증정보를 가지고 있는 서비스가 만드는 어플리케이션에서만 사용할 수 있는 방식이다.
     * 장점은 응답으로 바로 access token을 받을 수 있다는 것이다.
     * 지금 우리도 account정보를 직접 가지고 있으므로 Password 타입을 사용하는 인증 방식을 써도 된다.
     */
    @Test
    @TestDescription("인증 토큰을 발급 받는 테스트")
    public void getAuthToken() throws Exception {
        // Given
        // Application Runner 에서 유저를 이미 하나 저장하므로 별도의 저장로직 필요없음

        // 인증서버가 등록이 되면 기본적으로 '/oauth/token' 요청을 처리할 수 있는 핸들러가 등록된다.
        this.mockMvc.perform(
            post("/oauth/token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))    //HTTP basic 인증 헤더
                .param("username", appProperties.getUserUsername())
                .param("password", appProperties.getUserPassword())
                .param("grant_type", "password")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("access_token").exists());
    }

}