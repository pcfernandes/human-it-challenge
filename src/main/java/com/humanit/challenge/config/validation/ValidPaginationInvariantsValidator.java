package com.humanit.challenge.config.validation;

import com.humanit.challenge.config.AppPaginationProperties;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Enforces {@code defaultPageSize &lt;= maxPageSize} for {@link AppPaginationProperties}.
 * Nulls are treated leniently here; the member-level {@code @NotNull} constraints
 * classify nulls.
 */
public class ValidPaginationInvariantsValidator
        implements ConstraintValidator<ValidPaginationInvariants, AppPaginationProperties> {

    @Override
    public boolean isValid(AppPaginationProperties properties, ConstraintValidatorContext context) {
        if (properties == null) {
            return true;
        }
        Integer defaultPageSize = properties.defaultPageSize();
        Integer maxPageSize = properties.maxPageSize();
        if (defaultPageSize == null || maxPageSize == null) {
            return true;
        }
        if (defaultPageSize <= maxPageSize) {
            return true;
        }
        if (context != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("default-page-size must not exceed max-page-size")
                    .addConstraintViolation();
        }
        return false;
    }
}
