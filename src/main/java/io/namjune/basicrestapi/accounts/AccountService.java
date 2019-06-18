package io.namjune.basicrestapi.accounts;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 시큐리티 설정에서 AuthenticationManager 를 만들때 PasswordEncoder 를 사용하기 때문에
     * Account 를 만들때도 encoding이 필요하다.
     *
     * @param account
     * @return Account
     */
    public Account savePasswordEncodedAccount(Account account) {
        account.setPassword(this.passwordEncoder.encode(account.getPassword()));

        return accountRepository.save(account);
    }

    /**
     * 우리가 사용하는 도메인을 스프링 시큐리티가 사용하는 UserDetails 로 변환하는 작업
     *
     * @param username Account 이메일
     * @return UserDetails
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.accountRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException(username));

        // 스프링 시큐리티의 User 대신 Account 엔티티를 바로 받을 수 있게 하는 어댑터 리턴
        return new AccountAdapter(account);
    }
}
