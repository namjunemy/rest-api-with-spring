package io.namjune.basicrestapi.accounts;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class AccountServiceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Test
    public void findByUsername() {
        // Given
        String password = "1234";
        String email = "namjunemy@gmail.com";
        Account account = Account.builder()
            .email(email)
            .password(password)
            .roles(new HashSet<>(Arrays.asList(AccountRole.ADMIN, AccountRole.USER)))
            .build();

        this.accountRepository.save(account);

        // When
        UserDetailsService userDetailsService = this.accountService;
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Then
        assertThat(userDetails.getPassword()).isEqualTo(password);
    }

    /*
    예외 테스트 방법
    1. @Test(expected = UsernameNotFoundException.class) -> 예외 타입만 확인 가능

    2. try-catch 블록 -> 예외 타입과 메시지 확인 가능

        try {
            this.accountService.loadUserByUsername(username);
            fail("명시적으로 테스트를 실패하기 위함");

        } catch (UsernameNotFoundException e) {
            assertThat(e.getMessage()).containsSequence(username);
        }

    3. @Rule ExpectedException - 코드는 간결하면서 예외 타입과 메시지 모두 확인 가능
     */
    @Test
    public void findByUsernameFail() {
        // Given
        String username = "ramdom@gmail.com";

        // Expected
        // 주의할점은 예상되는 예외와 메세지를 미리 정의해 줘야 한다는 점. 예측을 하는 Exception 이므로.
        expectedException.expect(UsernameNotFoundException.class);
        expectedException.expectMessage(Matchers.containsString(username));

        // When
        this.accountService.loadUserByUsername(username);
    }
}