package com.humanit.challenge.dto.patch;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * A required PATCH property. An omitted property ({@link PatchValue.State#ABSENT}) is
 * accepted because the existing value is preserved; an explicit JSON {@code null}
 * ({@link PatchValue.State#PRESENT_NULL}) is rejected.
 */
@Documented
@Constraint(validatedBy = PatchRequiredValidator.class)
@Target({FIELD, METHOD, PARAMETER})
@Retention(RUNTIME)
public @interface PatchRequired {

    String message() default "must not be null";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
