package com.humanit.challenge.dto.patch;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Rejects explicit JSON {@code null} for required PATCH properties. */
public class PatchRequiredValidator implements ConstraintValidator<PatchRequired, PatchValue<?>> {

    @Override
    public boolean isValid(PatchValue<?> value, ConstraintValidatorContext context) {
        // A null holder is a programming error, not a JSON state; it is rejected as well.
        return value != null && !value.isPresentNull();
    }
}
