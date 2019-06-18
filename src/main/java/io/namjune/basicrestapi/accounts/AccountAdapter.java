package io.namjune.basicrestapi.accounts;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;


/**
 * 컨트롤러에서 createEvent를 수행할 때 스프링 시큐리티의 User를 Account에 바로 등록할 수 없다.
 * Account를 Event의 manager로 등록하기 위해서 UserDetails를 리턴하는 AccountService의 loadUserByUsername()에
 * User를 상속 받은 AccountAdapter를 리턴하도록 만들면, 컨트롤러에서 바로 Account 엔티티를 꺼낼 수 있게 된다.
 */
public class AccountAdapter extends User {

    private Account account;

    public AccountAdapter(Account account) {
        super(account.getEmail(), account.getPassword(), authorities(account.getRoles()));
        this.account = account;
    }

    // Account 의 role 정보를 가지고 authorities 정보를 만든다.
    private static Collection<? extends GrantedAuthority> authorities(Set<AccountRole> roles) {
        return roles.stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
            .collect(Collectors.toSet());
    }

    public Account getAccount() {
        return account;
    }
}
