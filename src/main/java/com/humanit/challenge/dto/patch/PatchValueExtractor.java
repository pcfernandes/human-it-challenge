package com.humanit.challenge.dto.patch;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

/**
 * Bean Validation container support for {@link PatchValue}.
 *
 * <p>Only a {@link PatchValue.State#PRESENT_VALUE} is exposed to the validator. Constraints
 * placed on the type argument, for example {@code PatchValue<@NotBlank @Size(max = 100) String>}
 * or {@code PatchValue<List<@Valid ClientDocumentPatchRequest>>}, therefore apply to supplied
 * values only. {@link PatchValue.State#ABSENT} and {@link PatchValue.State#PRESENT_NULL} expose
 * nothing; required-ness is expressed separately with {@link PatchRequired}.
 *
 * <p>Registered for discovery through
 * {@code META-INF/services/jakarta.validation.valueextraction.ValueExtractor}.
 */
public class PatchValueExtractor implements ValueExtractor<PatchValue<@ExtractedValue ?>> {

    @Override
    public void extractValues(PatchValue<?> originalValue, ValueExtractor.ValueReceiver receiver) {
        if (originalValue != null && originalValue.isPresentValue()) {
            receiver.value(null, originalValue.get());
        }
    }
}
