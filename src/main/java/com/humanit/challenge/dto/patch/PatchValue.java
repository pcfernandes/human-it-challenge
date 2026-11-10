package com.humanit.challenge.dto.patch;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Project-owned tri-state holder for PATCH request properties.
 *
 * <ul>
 *   <li>{@link State#ABSENT}: the JSON property was omitted; the existing value is preserved.</li>
 *   <li>{@link State#PRESENT_NULL}: the JSON property was explicitly {@code null}.</li>
 *   <li>{@link State#PRESENT_VALUE}: the JSON property carried a non-null value.</li>
 * </ul>
 *
 * <p>Jackson binding is provided by {@link PatchValueDeserializer}, attached to this class so
 * that no global ObjectMapper configuration is required. Bean Validation constraints are applied
 * to the type argument and evaluated only for {@link State#PRESENT_VALUE} through
 * {@link PatchValueExtractor}.
 *
 * @param <T> the wrapped value type
 */
@JsonDeserialize(using = PatchValueDeserializer.class)
public final class PatchValue<T> {

    /** The three distinguishable PATCH property states. */
    public enum State {
        ABSENT,
        PRESENT_NULL,
        PRESENT_VALUE
    }

    private static final PatchValue<?> ABSENT = new PatchValue<>(State.ABSENT, null);
    private static final PatchValue<?> PRESENT_NULL = new PatchValue<>(State.PRESENT_NULL, null);

    private final State state;
    private final T value;

    private PatchValue(State state, T value) {
        this.state = state;
        this.value = value;
    }

    /** Property omitted from the request. */
    @SuppressWarnings("unchecked")
    public static <T> PatchValue<T> absent() {
        return (PatchValue<T>) ABSENT;
    }

    /** Property explicitly set to JSON {@code null}. */
    @SuppressWarnings("unchecked")
    public static <T> PatchValue<T> presentNull() {
        return (PatchValue<T>) PRESENT_NULL;
    }

    /**
     * Property supplied with a value. A {@code null} argument is treated as an explicit
     * {@code null} and yields {@link State#PRESENT_NULL}, never {@link State#ABSENT}.
     */
    public static <T> PatchValue<T> of(T value) {
        if (value == null) {
            return presentNull();
        }
        return new PatchValue<>(State.PRESENT_VALUE, value);
    }

    public State state() {
        return state;
    }

    public boolean isAbsent() {
        return state == State.ABSENT;
    }

    public boolean isPresentNull() {
        return state == State.PRESENT_NULL;
    }

    public boolean isPresentValue() {
        return state == State.PRESENT_VALUE;
    }

    /**
     * Returns the supplied value.
     *
     * @throws IllegalStateException when the state is not {@link State#PRESENT_VALUE}
     */
    public T get() {
        if (state != State.PRESENT_VALUE) {
            throw new IllegalStateException("PATCH value is " + state + "; no value is available");
        }
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PatchValue<?> that)) {
            return false;
        }
        return state == that.state && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, value);
    }

    /** Reports only the state so that potentially sensitive values never reach logs. */
    @Override
    public String toString() {
        return "PatchValue[" + state + "]";
    }
}
