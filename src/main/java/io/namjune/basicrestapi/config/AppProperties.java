package io.namjune.basicrestapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;

@Component
@ConfigurationProperties(prefix = "my-app")
@Getter
@Setter
public class AppProperties {

    @NotEmpty
    private String AdminUsername;

    @NotEmpty
    private String AdminPassword;

    @NotEmpty
    private String UserUsername;

    @NotEmpty
    private String UserPassword;

    @NotEmpty
    private String ClientId;

    @NotEmpty
    private String ClientSecret;
}
