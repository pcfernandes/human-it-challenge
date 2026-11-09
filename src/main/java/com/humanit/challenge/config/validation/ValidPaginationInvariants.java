package com.humanit.challenge.config.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Class-level invariant for the pagination configuration:
 * {@code default-page-size} must not exceed {@code max-page-size}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Constraint(validatedBy = ValidPaginationInvariantsValidator.class)
public @interface ValidPaginationInvariants {

    String message() default "pagination configuration is inconsistent";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
