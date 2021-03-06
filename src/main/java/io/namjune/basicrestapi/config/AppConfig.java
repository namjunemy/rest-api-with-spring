package io.namjune.basicrestapi.config;

import io.namjune.basicrestapi.accounts.Account;
import io.namjune.basicrestapi.accounts.AccountRole;
import io.namjune.basicrestapi.accounts.AccountService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    /**
     * 다양한 인코딩 타입을 지원하는 패스워드 인코더
     * 인코딩 된 패스워드 앞에 prefix 를 붙여준다.
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ApplicationRunner applicationRunner() {
        return new ApplicationRunner() {

            @Autowired
            AccountService accountService;

            @Autowired
            AppProperties appProperties;

            @Override
            public void run(ApplicationArguments args) throws Exception {
                Account admin = Account.builder()
                    .email(appProperties.getAdminUsername())
                    .password(appProperties.getAdminPassword())
                    .roles(Stream.of(AccountRole.ADMIN, AccountRole.USER).collect(Collectors.toSet()))
                    .build();
                accountService.savePasswordEncodedAccount(admin);

                Account user = Account.builder()
                    .email(appProperties.getUserUsername())
                    .password(appProperties.getUserPassword())
                    .roles(Stream.of(AccountRole.USER).collect(Collectors.toSet()))
                    .build();
                accountService.savePasswordEncodedAccount(user);
            }
        };
    }
}
