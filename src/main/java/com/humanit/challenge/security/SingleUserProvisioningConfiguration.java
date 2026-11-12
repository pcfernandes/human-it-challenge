package com.humanit.challenge.security;

import java.util.regex.Pattern;

import com.humanit.challenge.config.AppDemoUserProperties;
import com.humanit.challenge.config.AppProductionUserProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.util.StringUtils;

/** Provides exactly one BCrypt-backed in-memory user from the active profile's credentials. */
@Configuration(proxyBeanMethods = false)
public class SingleUserProvisioningConfiguration {

    private static final Pattern BCRYPT_HASH = Pattern.compile("^\\$2[aby]\\$(0[4-9]|[12][0-9]|3[01])\\$[./A-Za-z0-9]{53}$");

    @Bean
    @Profile("dev & !prod")
    UserDetailsService demoUserDetailsService(AppDemoUserProperties properties) {
        return inMemoryUser(properties.username(), properties.passwordHash(), "app.security.demo-user");
    }

    @Bean
    @Profile("prod")
    UserDetailsService productionUserDetailsService(AppProductionUserProperties properties) {
        return inMemoryUser(properties.username(), properties.passwordHash(), "app.security.user");
    }

    private UserDetailsService inMemoryUser(String username, String passwordHash, String propertyPrefix) {
        if (!StringUtils.hasText(username) || !isBcryptHash(passwordHash)) {
            throw new IllegalStateException(propertyPrefix
                    + ".username and " + propertyPrefix + ".password-hash must contain a username and BCrypt hash");
        }

        return new InMemoryUserDetailsManager(User.withUsername(username)
                .password(passwordHash)
                .build());
    }

    private boolean isBcryptHash(String value) {
        return value != null && BCRYPT_HASH.matcher(value).matches();
    }
}
