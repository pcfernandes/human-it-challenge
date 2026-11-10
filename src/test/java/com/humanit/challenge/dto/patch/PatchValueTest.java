package com.humanit.challenge.dto.patch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** Unit tests for the tri-state {@link PatchValue} holder, independent of Jackson. */
class PatchValueTest {

    @Test
    void absentIsOnlyAbsent() {
        PatchValue<String> value = PatchValue.absent();

        assertThat(value.state()).isEqualTo(PatchValue.State.ABSENT);
        assertThat(value.isAbsent()).isTrue();
        assertThat(value.isPresentNull()).isFalse();
        assertThat(value.isPresentValue()).isFalse();
    }

    @Test
    void presentNullIsOnlyPresentNull() {
        PatchValue<String> value = PatchValue.presentNull();

        assertThat(value.state()).isEqualTo(PatchValue.State.PRESENT_NULL);
        assertThat(value.isAbsent()).isFalse();
        assertThat(value.isPresentNull()).isTrue();
        assertThat(value.isPresentValue()).isFalse();
    }

    @Test
    void ofNonNullIsPresentValue() {
        PatchValue<String> value = PatchValue.of("Ana");

        assertThat(value.state()).isEqualTo(PatchValue.State.PRESENT_VALUE);
        assertThat(value.isPresentValue()).isTrue();
        assertThat(value.get()).isEqualTo("Ana");
    }

    @Test
    void ofNullIsTreatedAsExplicitNullNeverAbsent() {
        PatchValue<String> value = PatchValue.of(null);

        assertThat(value.state()).isEqualTo(PatchValue.State.PRESENT_NULL);
        assertThat(value).isEqualTo(PatchValue.presentNull());
    }

    @Test
    void getFailsWhenNoValueIsPresent() {
        assertThatThrownBy(() -> PatchValue.absent().get()).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> PatchValue.presentNull().get()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equalityDistinguishesStates() {
        assertThat(PatchValue.absent()).isNotEqualTo(PatchValue.presentNull());
        assertThat(PatchValue.presentNull()).isNotEqualTo(PatchValue.of("x"));
        assertThat(PatchValue.of("x")).isEqualTo(PatchValue.of("x"));
        assertThat(PatchValue.of("x")).isNotEqualTo(PatchValue.of("y"));
    }

    @Test
    void toStringReportsStateWithoutValue() {
        assertThat(PatchValue.of("secret-value").toString())
                .isEqualTo("PatchValue[PRESENT_VALUE]")
                .doesNotContain("secret-value");
    }

    @Test
    void exactlyThreeStatesExist() {
        assertThat(PatchValue.State.values())
                .containsExactly(PatchValue.State.ABSENT, PatchValue.State.PRESENT_NULL,
                        PatchValue.State.PRESENT_VALUE);
    }
}
