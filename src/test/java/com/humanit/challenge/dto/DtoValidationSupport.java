package com.humanit.challenge.dto;

import java.lang.annotation.Annotation;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * Shared Bean Validation helper for DTO contract tests. Uses the default
 * {@link Validator} without a Spring context, so the tests stay fast and independent of
 * persistence, security and Jackson PATCH infrastructure.
 */
public final class DtoValidationSupport {

    private static final Validator VALIDATOR;

    static {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            VALIDATOR = factory.getValidator();
        }
    }

    private DtoValidationSupport() {
    }

    public static <T> Set<ConstraintViolation<T>> validate(T target) {
        return VALIDATOR.validate(target);
    }

    /**
     * True when a violation exists for the given property path (for example
     * {@code "firstName"} or {@code "documents[0].number"}) raised by the given constraint.
     */
    public static boolean hasViolation(Set<? extends ConstraintViolation<?>> violations,
            String propertyPath, Class<? extends Annotation> constraint) {
        return violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals(propertyPath)
                        && v.getConstraintDescriptor().getAnnotation().annotationType() == constraint);
    }

    /** True when any violation exists for the given property path, regardless of constraint. */
    public static boolean hasViolationOn(Set<? extends ConstraintViolation<?>> violations, String propertyPath) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(propertyPath));
    }
}
