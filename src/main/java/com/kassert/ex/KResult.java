package com.kassert.ex;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Result type modelled after Rust's {@code Result<T, E>} where {@code E} is
 * always {@link RuntimeException}.
 *
 * <p>
 * A {@code KResult<T>} is either:
 * <ul>
 * <li><em>Ok</em> – carries a value of type {@code T}.</li>
 * <li><em>Err</em> – carries a {@link RuntimeException} describing the
 * failure.</li>
 * </ul>
 *
 * <p>
 * Create instances with {@link #ok(Object)} and {@link #err(RuntimeException)}.
 * Query the variant with {@link #ok()} / {@link #err()}, extract values
 * with {@link #get()} / {@link #getOr(Object)}, and chain operations with
 * {@link #map(Function)}, {@link #andThen(Function)}, etc.
 *
 * @param <T> the Ok value type
 */
public final class KResult<T>
{
    /** Non-null value on the Ok path; may be {@code null} for Ok(null). */
    private final T value;

    /**
     * The type of the Ok value, used for type inference in err() factory method.
     */
    private final Class<T> type;

    /** Non-null exception on the Err path; {@code null} when Ok. */
    private final RuntimeException error;

    // -------------------------------------------------------------------------
    // Private constructor
    // -------------------------------------------------------------------------

    private KResult(final Class<T> type, final T value, final RuntimeException error)
    {
        this.value = value;
        this.type = type;
        this.error = error;
    }

    // -------------------------------------------------------------------------
    // Factory methods (Rust: Ok(v) / Err(e))
    // -------------------------------------------------------------------------

    /**
     * Creates an empty Ok result with a dummy value. Useful for cases where the
     * presence of a value is not important, and only success/failure matters.
     *
     * @param <T> the Ok value type
     * @return an Ok {@code KResult} with a dummy value
     */
    @SuppressWarnings("unchecked") // Safe: The dummy value is never accessed, so its actual type is irrelevant. We
                                   // use Object.class to satisfy the type parameter, but it will never be returned
                                   // or cast to T.
    public static <T> KResult<T> empty()
    {
        return new KResult<T>((Class<T>) Object.class, null, null);
    }

    /**
     * Creates an Ok result containing {@code value}.
     *
     * @param <T>   the Ok value type
     * @param value the success value (may be {@code null})
     * @return an Ok {@code KResult}
     */
    @SuppressWarnings("unchecked") // Safe: value.getClass() returns Class<? extends T>, which can be safely cast
                                   // to Class<T> for the purpose of type inference in err() factory method.
    public static <T> KResult<T> ok(final T value)
    {
        Objects.requireNonNull(value, "Ok value must not be null");
        return new KResult<T>((Class<T>) value.getClass(), value, null);
    }

    /**
     * Creates an Err result carrying {@code error}.
     *
     * @param error the failure exception; must not be {@code null}
     * @return an Err {@code KResult}
     */
    public static <T> KResult<T> err(final Class<T> type, final RuntimeException error)
    {
        Objects.requireNonNull(type, "Type must not be null");
        Objects.requireNonNull(error, "Exception must not be null");
        return new KResult<T>(type, (T) null, error);
    }

    // -------------------------------------------------------------------------
    // State queries (Rust: is_ok, is_err, is_ok_and, is_err_and)
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if this result is Ok.
     *
     * @return {@code true} when Ok
     */
    public boolean ok()
    {
        return error == null;
    }

    /**
     * Returns {@code true} if this result is Err.
     *
     * @return {@code true} when Err
     */
    public boolean err()
    {
        return error != null;
    }

    /**
     * Returns {@code true} if this result is Ok and {@code predicate} holds for the
     * contained value.
     *
     * @param predicate the predicate to test; must not be {@code null}
     * @return {@code true} when Ok and {@code predicate.test(value)} is
     *         {@code true}
     * @throws IllegalArgumentException if {@code predicate} is {@code null}
     */
    public boolean isOkAnd(final Predicate<T> predicate)
    {
        Objects.requireNonNull(predicate, "predicate must not be null");
        if (!ok()) return false;
        return predicate.test(value);
    }

    /**
     * Returns {@code true} if this result is Err and {@code predicate} holds for
     * the contained exception.
     *
     * @param predicate the predicate to test; must not be {@code null}
     * @return {@code true} when Err and {@code predicate.test(error)} is
     *         {@code true}
     * @throws IllegalArgumentException if {@code predicate} is {@code null}
     */
    public boolean isErrAnd(final Predicate<RuntimeException> predicate)
    {
        Objects.requireNonNull(predicate, "predicate must not be null");
        if (!err()) return false;
        return predicate.test(error);
    }

    // -------------------------------------------------------------------------
    // Value extraction (Rust: unwrap, unwrap_err, expect, expect_err,
    // unwrap_or, unwrap_or_else)
    // -------------------------------------------------------------------------

    /**
     * Returns the Ok value, or throws the Err exception if this result is Err.
     *
     * @return the Ok value
     * @throws RuntimeException if this result is Err
     */
    public T get()
    {
        if (err()) throw error;
        return value;
    }

    /**
     * Returns the Err exception, or throws if this result is Ok.
     *
     * @return the Err exception
     * @throws RuntimeException if this result is Ok
     */
    public RuntimeException getErr()
    {
        return expectErr("called getErr() on an Ok value");
    }

    /**
     * Returns the Ok value, or throws with {@code message} as the detail if this
     * result is Err.
     *
     * @param message the detail message for the thrown exception; must not be
     *                {@code null}
     * @return the Ok value
     * @throws RuntimeException         if this result is Err
     * @throws IllegalArgumentException if {@code message} is {@code null}
     */
    public T expect(final String message)
    {
        Objects.requireNonNull(message, "expect() message must not be null");
        if (err()) throw new IllegalStateException(message, error);
        return value;
    }

    /**
     * Returns the Err exception, or throws with {@code message} as the detail if
     * this result is Ok.
     *
     * @param message the detail message for the thrown exception; must not be
     *                {@code null}
     * @return the Err exception
     * @throws IllegalStateException         if this result is Ok
     * @throws IllegalArgumentException if {@code message} is {@code null}
     */
    public RuntimeException expectErr(final String message)
    {
        Objects.requireNonNull(message, "expectErr() message must not be null");
        if (ok()) throw new IllegalStateException(message + ": " + value);
        return error;
    }

    /**
     * Returns the Ok value, or {@code defaultValue} if this result is Err.
     *
     * @param defaultValue the fallback value
     * @return the Ok value, or {@code defaultValue}
     */
    public T getOr(final T defaultValue)
    {
        if (ok()) return value;
        return defaultValue;
    }

    /**
     * Returns the Ok value, or computes a default from the Err exception using
     * {@code fn}.
     *
     * @param fn the fallback function; must not be {@code null}
     * @return the Ok value, or the result of {@code fn.apply(error)}
     * @throws IllegalArgumentException if {@code fn} is {@code null}
     */
    public T getOrElse(final Function<RuntimeException, T> fn)
    {
        Objects.requireNonNull(fn, "fn must not be null");
        if (ok()) return value;
        return fn.apply(error);
    }

    // -------------------------------------------------------------------------
    // Transformation (Rust: map, map_err, map_or, map_or_else)
    // -------------------------------------------------------------------------

    /**
     * Applies {@code mapper} to the Ok value and wraps the result in Ok, or
     * propagates the Err unchanged.
     *
     * @param <U>    the mapped Ok value type
     * @param mapper the mapping function; must not be {@code null}
     * @return the mapped Ok result, or the original Err as {@code KResult<U>}
     */
    @SuppressWarnings("unchecked") // Safe: KResult is final; type erasure makes KResult<T> indistinguishable from
                                   // KResult<U> at runtime.
    public <U> KResult<? extends U> map(final Function<T, U> mapper)
    {
        Objects.requireNonNull(mapper, "mapper must not be null");
        if (!ok()) return err((Class<U>) Object.class, error);
        U mapped = mapper.apply(value);
        return mapped == null ? empty() : ok(mapped);
    }

    /**
     * Applies {@code mapper} to the Err exception and wraps the result in Err, or
     * propagates the Ok unchanged.
     *
     * @param mapper the error-mapping function; must not be {@code null}
     * @return the mapped Err result, or the original Ok
     */
    public KResult<T> mapErr(final Function<RuntimeException, RuntimeException> mapper)
    {
        Objects.requireNonNull(mapper, "mapper must not be null");
        if (!err()) return this;
        RuntimeException mapped = mapper.apply(error);
        Objects.requireNonNull(mapped, "mapper must not return null");
        return err(type, mapped);
    }

    /**
     * Returns {@code defaultValue} if Err, or applies {@code mapper} to the Ok
     * value.
     * @param mapper       the mapping function; must not be {@code null}
     * @param defaultValue the fallback value
     *
     * @param <U>          the result type
     * @return the mapped Ok value, or {@code defaultValue}
     * @throws IllegalArgumentException if {@code mapper} is {@code null}
     */
    public <U> U mapOr(final Function<T, U> mapper, final U defaultValue)
    {
        return mapOrElse(e -> defaultValue, mapper);
    }

    /**
     * Applies {@code okMapper} to the Ok value, or {@code errMapper} to the Err
     * exception.
     *
     * @param <U>       the result type
     * @param errMapper the function applied to the Err exception; must not be
     *                  {@code null}
     * @param okMapper  the function applied to the Ok value; must not be
     *                  {@code null}
     * @return the result of the appropriate mapping function
     * @throws IllegalArgumentException if either mapper is {@code null}
     */
    public <U> U mapOrElse(final Function<RuntimeException, U> errMapper, final Function<T, U> okMapper)
    {
        Objects.requireNonNull(errMapper, "errMapper must not be null");
        Objects.requireNonNull(okMapper, "okMapper must not be null");
        if (ok()) return okMapper.apply(value);
        return errMapper.apply(error);
    }

    // -------------------------------------------------------------------------
    // Boolean AND chaining (Rust: and, and_then)
    // -------------------------------------------------------------------------

    /**
     * Returns {@code other} if this result is Ok, or propagates this Err.
     *
     * @param <U>   the Ok value type of the other result
     * @param other the result to return when Ok; must not be {@code null}
     * @return {@code other} when Ok, or this Err propagated as {@code KResult<U>}
     */
    @SuppressWarnings("unchecked") // Safe: KResult is final; type erasure makes KResult<U> indistinguishable from
                                   // KResult<T> at runtime.
    public <U> KResult<U> and(final KResult<U> other)
    {
        Objects.requireNonNull(other, "other must not be null");
        if (!ok()) return err((Class<U>) Object.class, error);
        return other;
    }

    /**
     * Returns the result of applying {@code fn} to the Ok value, or propagates this
     * Err.
     *
     * @param <U> the Ok value type returned by {@code fn}
     * @param fn  the chaining function; must not be {@code null}
     * @return the result of {@code fn}, or this Err propagated as
     *         {@code KResult<U>}
     */
    @SuppressWarnings("unchecked") // Safe: KResult is final; type erasure makes KResult<U> indistinguishable from
                                   // KResult<T> at runtime.
    public <U> KResult<? extends U> andThen(final Function<T, KResult<? extends U>> fn)
    {
        Objects.requireNonNull(fn, "fn must not be null");
        if (!ok()) return err((Class<U>) Object.class, error);
        final KResult<? extends U> result = fn.apply(value);
        Objects.requireNonNull(result, "andThen function must not return null");
        return result;
    }

    // -------------------------------------------------------------------------
    // Boolean OR chaining (Rust: or, or_else)
    // -------------------------------------------------------------------------

    /**
     * Returns this result if Ok, or {@code other} if Err.
     *
     * <p>
     * Accepts {@code KResult<? extends T>} (covariant) so that a
     * {@code KResult<Integer>} can serve as fallback for a {@code KResult<Number>},
     * and so that {@code err(e)} constructed via static import is accepted without
     * an explicit type witness.
     *
     * @param other the fallback result; must not be {@code null}
     * @return this result if Ok, or {@code other} coerced to {@code KResult<T>}
     * @throws IllegalArgumentException if {@code other} is {@code null}
     */
    public KResult<? extends T> or(final KResult<? extends T> other)
    {
        return orElse(e -> other);
    }

    /**
     * Returns this result if Ok, or applies {@code fn} to the Err exception.
     *
     * <p>
     * Accepts a function returning {@code ? extends KResult<? extends T>}
     * (covariant) for the same reasons as {@link #or(KResult)}: allows subtype
     * results and {@code err(e)} via static import without an explicit type
     * witness.
     *
     * @param fn the recovery function; must not be {@code null}
     * @return this result if Ok, or the result of {@code fn} coerced to
     *         {@code KResult<T>}
     * @throws IllegalArgumentException if {@code fn} is {@code null}
     */
    public KResult<? extends T> orElse(final Function<RuntimeException, ? extends KResult<? extends T>> fn)
    {
        Objects.requireNonNull(fn, "fn must not be null");
        if (ok()) return this;
        final KResult<? extends T> result = fn.apply(error);
        Objects.requireNonNull(result, "orElse function must not return null");
        return result;
    }

    // -------------------------------------------------------------------------
    // Side-effect inspection (Rust: inspect, inspect_err)
    // -------------------------------------------------------------------------

    /**
     * Calls {@code consumer} with the Ok value if this result is Ok, then returns
     * this result unchanged.
     *
     * <p>
     * Intended for side effects that observe the value (e.g. logging) without
     * modifying it. Mirrors Rust's {@code Result::inspect}, where the closure
     * receives an immutable reference ({@code &T}). Callers must not mutate the
     * contained value through the consumer.
     *
     * @param consumer the observing consumer; must not be {@code null}
     * @return this result
     * @throws IllegalArgumentException if {@code consumer} is {@code null}
     */
    public KResult<T> inspect(final Consumer<T> consumer)
    {
        Objects.requireNonNull(consumer, "consumer must not be null");
        if (ok()) consumer.accept(value);
        return this;
    }

    /**
     * Calls {@code consumer} with the Err exception if this result is Err, then
     * returns this result unchanged.
     *
     * <p>
     * Intended for side effects that observe the exception (e.g. logging) without
     * modifying it. Mirrors Rust's {@code Result::inspect_err}, where the closure
     * receives an immutable reference ({@code &E}). Callers must not mutate the
     * contained exception through the consumer.
     *
     * @param consumer the observing consumer; must not be {@code null}
     * @return this result
     * @throws IllegalArgumentException if {@code consumer} is {@code null}
     */
    public KResult<T> inspectErr(final Consumer<RuntimeException> consumer)
    {
        Objects.requireNonNull(consumer, "consumer must not be null");
        if (err()) consumer.accept(error);
        return this;
    }
}
