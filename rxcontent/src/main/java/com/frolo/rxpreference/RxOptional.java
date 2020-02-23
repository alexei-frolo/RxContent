package com.frolo.rxpreference;


import java.util.NoSuchElementException;

/**
 * A container object that may or may not contain a non-null value.
 * Actually, this is same class as java.util.Optional from Java8.
 * @param <T> the class of the value
 */
public final class RxOptional<T> {

    private static final RxOptional EMPTY = new RxOptional();

    private final T value;

    static <T> RxOptional<T> empty() {
        return EMPTY;
    }

    static <T> RxOptional<T> of(T value) {
        return new RxOptional<>(value);
    }

    static <T> RxOptional<T> ofNullable(T value) {
        return value != null ? of(value) : RxOptional.<T>empty();
    }

    private RxOptional() {
        value = null;
    }

    private RxOptional(T value) {
        if (value == null) {
            throw new NullPointerException("Value cannot be null");
        }
        this.value = value;
    }

    /**
     * If a value is present in this {@link RxOptional}, returns the value,
     * otherwise throws {@code NoSuchElementException}.
     *
     * @return the non-null value held by this {@code Optional}
     * @throws NoSuchElementException if there is no value present
     *
     * @see RxOptional#isPresent()
     */
    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    /**
     * Returns {@code true} if there is a value present, otherwise {@code false}.
     *
     * @return {@code true} if there is a value present, otherwise {@code false}
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * Returns the value if present, otherwise returns {@code other}.
     * @param other the value to be returned if there is no value present, may
     * @return the value, if present, otherwise {@code other}
     */
    public T orElse(T other) {
        return value != null ? value : other;
    }

    @Override
    public String toString() {
        return value != null
                ? String.format("RxOptional[%s]", value)
                : "RxOptional.empty";
    }
}
