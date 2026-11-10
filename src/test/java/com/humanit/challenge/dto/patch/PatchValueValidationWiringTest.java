package com.humanit.challenge.dto.patch;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.humanit.challenge.dto.client.ClientPatchRequest;

import jakarta.validation.ConstraintViolation;

/**
 * Proves that the {@link PatchValueExtractor} registered through
 * {@code META-INF/services} is active in the Spring-managed validator, which is the validator
 * the application uses for {@code @Valid} request bodies.
 */
class PatchValueValidationWiringTest {

    @Test
    void springValidatorAppliesPatchValueTypeArgumentConstraints() throws Exception {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        try {
            ClientPatchRequest request = new ClientPatchRequest();
            request.setVersion(0L);
            request.setFirstName(PatchValue.of("   "));
            request.setEmail(PatchValue.of("not-an-email"));

            Set<ConstraintViolation<ClientPatchRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactlyInAnyOrder("firstName", "email");
        } finally {
            validator.close();
        }
    }

    @Test
    void springValidatorIgnoresAbsentAndPresentNullTypeArgumentConstraints() throws Exception {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        try {
            ClientPatchRequest request = new ClientPatchRequest();
            request.setVersion(0L);
            request.setDocuments(PatchValue.absent());
            request.setLastName(PatchValue.absent());

            assertThat(validator.validate(request)).isEmpty();
        } finally {
            validator.close();
        }
    }
}
