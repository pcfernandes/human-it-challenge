package com.humanit.challenge.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import com.humanit.challenge.config.validation.ValidPaginationInvariants;

/**
 * Approved pagination defaults and the hard maximum page size.
 *
 * <p>The defaults (page 0, size 20) and the ceiling (max 100) are approved values
 * (T1.3). The ceiling is code-owned: any environment override above 100 fails
 * startup at bind time. HTTP <code>page</code>/<code>size</code> request-parameter
 * validation belongs to T2.8 and is deliberately not implemented here.</p>
 *
 * <p>Immutable, constructor-bound record; validated by Jakarta Bean Validation.</p>
 */
@Validated
@ValidPaginationInvariants
@ConfigurationProperties(prefix = "app.pagination")
public record AppPaginationProperties(
        @NotNull @Min(0) Integer defaultPage,
        @NotNull @Min(1) Integer defaultPageSize,
        @NotNull @Min(1) @Max(100) Integer maxPageSize) {
}
