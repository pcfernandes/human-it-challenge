package com.humanit.challenge;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.humanit.challenge.config.AppJwtProperties;
import com.humanit.challenge.config.AppPaginationProperties;
import com.humanit.challenge.config.StartupTestHarness;

/**
 * Bootstrap sanity check. Deliberately has <strong>no</strong> {@code @ActiveProfiles},
 * so it exercises the native default-dev path end-to-end (default profile semantics,
 * Revision 3 Section 10.8). Per T1.3, dev requires the externalized JWT secret and the
 * demo-user credentials, supplied here via deterministic dummy test values.
 */
@SpringBootTest
class HumanItChallengeApplicationTests {

    @DynamicPropertySource
    static void supplyExternals(DynamicPropertyRegistry registry) {
        StartupTestHarness.supplyDevExternals(registry);
    }

    @Autowired
    private AppPaginationProperties pagination;

    @Autowired
    private AppJwtProperties jwt;

    @Test
    void contextLoads() {
        assertThat(pagination.maxPageSize()).isEqualTo(100);
        assertThat(jwt.expirationMinutes()).isEqualTo(60);
    }
}
